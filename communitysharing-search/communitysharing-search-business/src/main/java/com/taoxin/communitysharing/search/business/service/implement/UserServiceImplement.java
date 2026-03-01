package com.taoxin.communitysharing.search.business.service.implement;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.taoxin.communitysharing.common.exception.BusinessException;
import com.taoxin.communitysharing.common.response.PageResponse;
import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.search.business.domain.mapper.SelectMapper;
import com.taoxin.communitysharing.search.business.enums.ResponseStatusEnum;
import com.taoxin.communitysharing.search.business.index.UserIndex;
import com.taoxin.communitysharing.search.business.model.vo.req.SearchUserReqVo;
import com.taoxin.communitysharing.search.business.model.vo.res.SearchUserResVo;
import com.taoxin.communitysharing.search.business.service.UserServer;
import com.taoxin.communitysharing.common.uitl.NumberUtil;
import com.taoxin.communitysharing.search.dto.request.RebuildUserDocReqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImplement implements UserServer {
    @Resource
    private RestHighLevelClient restHighLevelClient;
    @Resource
    private SelectMapper selectMapper;

    /**
     * 搜索用户
     * @思路：
     * 1.
     */
    @Override
    public PageResponse<SearchUserResVo> searchUser(SearchUserReqVo reqVo) {
        // 查询关键字
        String keyword = reqVo.getKeyword();
        // 当前页码
        Integer pageNo = reqVo.getPageNo();

        // 构建查询，指定索引
        SearchRequest searchRequest = new SearchRequest(UserIndex.NAME);

        // 构建查询内容
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // 构建 multi_match 查询，查询 nickname 和 communitysharing_id 字段
        searchSourceBuilder.query(QueryBuilders.multiMatchQuery(
                keyword,
                UserIndex.FIELD_NICKNAME,
                UserIndex.FIELD_NICKNAME_PREFIX,
                UserIndex.FIELD_COMMUNITYSHARING_ID
        ));
        // 排序，按 fans_total 降序
        SortBuilder<?> sortBuilder = new FieldSortBuilder(UserIndex.FIELD_FANS_COUNT).order(SortOrder.DESC);
        searchSourceBuilder.sort(sortBuilder); // 添加排序
        // 设置初始位置和页大小
        int pageSize = 10; // 每页10条数据
        if (pageNo == null || pageNo < 1) pageNo = 1;
        int from = (pageNo - 1) * pageSize; // 计算起始位置
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(pageSize);
        // 设置高亮字段
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field(UserIndex.FIELD_NICKNAME)
                .field(UserIndex.FIELD_COMMUNITYSHARING_ID)
                .preTags("<strong style='color:blue'>") // 设置高亮前缀
                .postTags("</strong>");
        searchSourceBuilder.highlighter(highlightBuilder);
        // 将查询内容添加到查询对象中
        searchRequest.source(searchSourceBuilder);
        List<SearchUserResVo> searchUserResVoList = null;
        // 获取总记录数
        long total = 0;
        try {
            log.info("搜索用户: {}", searchRequest);
            // 执行查询
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            // 处理结果
            total = searchResponse.getHits().getTotalHits().value;
            log.info("总记录数: {}", total);
            searchUserResVoList = Lists.newArrayList();
            // 获取结果的文档列表
            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits) {
                log.info("文档内容: {}", hit.getSourceAsString());
                // 获取所有字段
                Map<String,Object> sourceAsMap = hit.getSourceAsMap();
                // 提取值
                Long id = ((Number) sourceAsMap.get(UserIndex.FIELD_USER_ID)).longValue();
                String communitysharingId = (String) sourceAsMap.get(UserIndex.FIELD_COMMUNITYSHARING_ID);
                String nickname = (String) sourceAsMap.get(UserIndex.FIELD_NICKNAME);
                String avatar = (String) sourceAsMap.get(UserIndex.FIELD_AVATAR);
                Integer fansTotal = ((Number) sourceAsMap.get(UserIndex.FIELD_FANS_COUNT)).intValue();
                Integer contentTotal = ((Number) sourceAsMap.get(UserIndex.FIELD_CONTENT_COUNT)).intValue();
                String HighLightKeyword = null;
                if ( CollUtil.isNotEmpty(hit.getHighlightFields()) && hit.getHighlightFields().containsKey(UserIndex.FIELD_NICKNAME)) {
                    HighLightKeyword = hit.getHighlightFields().get(UserIndex.FIELD_NICKNAME).fragments()[0].toString();
                }else if (CollUtil.isNotEmpty(hit.getHighlightFields()) && hit.getHighlightFields().containsKey(UserIndex.FIELD_COMMUNITYSHARING_ID)) {
                    HighLightKeyword = hit.getHighlightFields().get(UserIndex.FIELD_COMMUNITYSHARING_ID).fragments()[0].toString();
                }
                // 构建 SearchUserResVo 对象
                SearchUserResVo searchUserResVo = SearchUserResVo.builder()
                        .userId(id)
                        .communitySharingId(communitysharingId)
                        .nickname(nickname)
                        .avatar(avatar)
                        .contentTotal(NumberUtil.formatNumberString(contentTotal))
                        .fansTotal(NumberUtil.formatNumberString(fansTotal))
                        .HighLightKeyword(HighLightKeyword)
                        .build();
                searchUserResVoList.add(searchUserResVo);
            }
        } catch (Exception e) {
            log.error("搜索用户失败", e);
        }
        return PageResponse.success(searchUserResVoList, pageNo, total);
    }

    @Override
    public Response<Long> rebuildDocument(RebuildUserDocReqDTO reqDTO) {
        Long userId = reqDTO.getUserId();
        List<Map<String, Object>> result = selectMapper.selectEsUserIndexData(userId);
        if (CollUtil.isEmpty(result)) throw new BusinessException(ResponseStatusEnum.USER_GET_FAIL);
        for (Map<String, Object> map : result) {
            IndexRequest indexRequest = new IndexRequest(UserIndex.NAME);
            indexRequest.id(String.valueOf(map.get(UserIndex.FIELD_USER_ID)));
            indexRequest.source(map);
            try {
                restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
            } catch (Exception e) {
                log.error("重建用户文档失败",e);
            }
        }
        return Response.success();
    }
}
