package com.taoxin.communitysharing.search.business.service.implement;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.google.common.collect.Lists;
import com.taoxin.communitysharing.common.constant.DateConstants;
import com.taoxin.communitysharing.common.exception.BusinessException;
import com.taoxin.communitysharing.common.response.PageResponse;
import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.search.business.domain.mapper.SelectMapper;
import com.taoxin.communitysharing.search.business.enums.ContentSortTypeEnum;
import com.taoxin.communitysharing.search.business.enums.ContentTimeRangeEnum;
import com.taoxin.communitysharing.search.business.enums.ResponseStatusEnum;
import com.taoxin.communitysharing.search.business.index.ContentIndex;
import com.taoxin.communitysharing.search.business.model.vo.req.SearchContentReqVo;
import com.taoxin.communitysharing.search.business.model.vo.res.SearchContentResVo;
import com.taoxin.communitysharing.search.business.service.ContentServer;
import com.taoxin.communitysharing.common.uitl.DateUtil;
import com.taoxin.communitysharing.common.uitl.NumberUtil;
import com.taoxin.communitysharing.search.dto.request.RebuildContentDocReqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.lucene.search.function.FieldValueFactorFunction;
import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class ContentServiceImplement implements ContentServer {
    @Resource
    private RestHighLevelClient restHighLevelClient;
    @Resource
    private SelectMapper selectMapper;

    @Override
    public PageResponse<SearchContentResVo> searchContent(SearchContentReqVo reqVo) {
        // 查询关键字
        String keyword = reqVo.getKeyword();
        // 当前页码
        Integer pageNo = reqVo.getPageNo();
        // 搜索类型
        Integer type = reqVo.getType();
        // 排序方式
        Integer sort = reqVo.getSort();
        // 时间范围
        Integer timeRange = reqVo.getTimeRange();

        SearchRequest searchRequest = new SearchRequest(ContentIndex.NAME);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 构建搜索条件
        BoolQueryBuilder multiMatchQueryBuilder = QueryBuilders.boolQuery().must(
                QueryBuilders.multiMatchQuery(keyword)
                        .field(ContentIndex.FIELD_CONTENT_TITLE,2.0f)
                        .field(ContentIndex.FIELD_CONTENT_TOPIC)
        );
        // 搜索类型
        if (Objects.nonNull(type)) {
            multiMatchQueryBuilder.filter(QueryBuilders.termQuery(ContentIndex.FIELD_CONTENT_TYPE, type));
        }
        // 时间范围
        if (Objects.nonNull(timeRange)) {
            ContentTimeRangeEnum contentTimeRangeEnum = ContentTimeRangeEnum.getByCode(timeRange);
            String startTime = null;
            String endTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateConstants.LOCAL_DATE_TIME_PATTERN));
            switch (contentTimeRangeEnum){
                case DAY -> startTime = DateUtil.LocalDateTimeToString(LocalDateTime.now().minusDays(1));
                case WEEK -> startTime = DateUtil.LocalDateTimeToString(LocalDateTime.now().minusDays(7));
                case MONTH -> startTime = DateUtil.LocalDateTimeToString(LocalDateTime.now().minusMonths(1));
                case HALF_YEAR -> startTime = DateUtil.LocalDateTimeToString(LocalDateTime.now().minusMonths(6));
            }
            // 设置时间范围
            if (StringUtils.isNotBlank(startTime)) {
                multiMatchQueryBuilder.filter(QueryBuilders.rangeQuery(ContentIndex.FIELD_CONTENT_CREATE_TIME).gte(startTime).lte(endTime));
            }
        }
        // 排序方式
        if (Objects.nonNull(sort)){
            ContentSortTypeEnum contentSortTypeEnum = ContentSortTypeEnum.getByCode(sort);
            switch (contentSortTypeEnum) {
                case UPDATE_TIME_DESC -> searchSourceBuilder.sort(new FieldSortBuilder(ContentIndex.FIELD_CONTENT_UPDATE_TIME).order(SortOrder.DESC));
                case LIKE_COUNT_DESC -> searchSourceBuilder.sort(new FieldSortBuilder(ContentIndex.FIELD_CONTENT_LIKE_TOTAL).order(SortOrder.DESC));
                case COMMENT_COUNT_DESC -> searchSourceBuilder.sort(new FieldSortBuilder(ContentIndex.FIELD_CONTENT_COMMENT_TOTAL).order(SortOrder.DESC));
                case COLLECT_COUNT_DESC -> searchSourceBuilder.sort(new FieldSortBuilder(ContentIndex.FIELD_CONTENT_COLLECT_TOTAL).order(SortOrder.DESC));
            }
            searchSourceBuilder.query(multiMatchQueryBuilder); // 搜索条件
        }else {
            searchSourceBuilder.sort(new FieldSortBuilder("_score").order(SortOrder.DESC));
            FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery(
                    multiMatchQueryBuilder,
                    new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{
                            // like_total 权重
                            new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                                    ScoreFunctionBuilders.fieldValueFactorFunction(ContentIndex.FIELD_CONTENT_LIKE_TOTAL)
                                            .factor(0.5f)
                                            .modifier(FieldValueFactorFunction.Modifier.SQRT)
                                            .missing(0)
                            ),
                            // collect_total 权重
                            new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                                    ScoreFunctionBuilders.fieldValueFactorFunction(ContentIndex.FIELD_CONTENT_COLLECT_TOTAL)
                                            .factor(0.3f)
                                            .modifier(FieldValueFactorFunction.Modifier.SQRT)
                                            .missing(0)
                            ),
                            // comment_total 权重
                            new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                                    ScoreFunctionBuilders.fieldValueFactorFunction(ContentIndex.FIELD_CONTENT_COMMENT_TOTAL)
                                            .factor(0.2f)
                                            .modifier(FieldValueFactorFunction.Modifier.SQRT)
                                            .missing(0)
                            ),
                            // 时间衰减
                            new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                                    ScoreFunctionBuilders.gaussDecayFunction(
                                            ContentIndex.FIELD_CONTENT_UPDATE_TIME,
                                            "now",
                                            "7d",
                                            null,
                                            0.5
                                    ).setWeight(0.8f)
                            )
                    }
            );
            // 设置 score_mode 和 boost_mode
            functionScoreQueryBuilder.scoreMode(FunctionScoreQuery.ScoreMode.SUM);
            functionScoreQueryBuilder.boostMode(CombineFunction.SUM);
            searchSourceBuilder.query(functionScoreQueryBuilder); // 设置查询
        }

        // 设置分页，from 和 size
        int pageSize = 10; // 每页展示数据量
        int from = (pageNo - 1) * pageSize; // 偏移量
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(pageSize);

        // 设置高亮字段
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field(ContentIndex.FIELD_CONTENT_TITLE)
                .preTags("<strong style='color:blue'>")
                .postTags("</strong>");
        searchSourceBuilder.highlighter(highlightBuilder);

        searchRequest.source(searchSourceBuilder); // 设置查询

        List<SearchContentResVo> searchContentResVoList = null;
        long total = 0;
        try {
            log.info("搜索内容: {}", searchSourceBuilder.toString());
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            // 处理结果
            total = searchResponse.getHits().getTotalHits().value;
            log.info("总记录数: {}", total);
            searchContentResVoList = Lists.newArrayList();
            // 获取结果的文档列表
            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits) {
                log.info("文档内容: {}", hit.getSourceAsString());
                // 获取所有字段
                Map<String,Object> sourceAsMap = hit.getSourceAsMap();
                Long contentId = ((Number) sourceAsMap.get(ContentIndex.FIELD_CONTENT_ID)).longValue();
                String cover = (String) sourceAsMap.get(ContentIndex.FIELD_CONTENT_COVER);
                String title = (String) sourceAsMap.get(ContentIndex.FIELD_CONTENT_TITLE);
                String nickname = (String) sourceAsMap.get(ContentIndex.FIELD_CONTENT_NICKNAME);
                String avatar = (String) sourceAsMap.get(ContentIndex.FIELD_CONTENT_AVATAR);
                String topic = (String) sourceAsMap.get(ContentIndex.FIELD_CONTENT_TOPIC);
                Object likeTotalObj = sourceAsMap.get(ContentIndex.FIELD_CONTENT_LIKE_TOTAL);
                Integer likeTotal = (likeTotalObj instanceof Number) ? ((Number) likeTotalObj).intValue() : 0;
                Object collectTotalObj = sourceAsMap.get(ContentIndex.FIELD_CONTENT_COLLECT_TOTAL);
                Integer collectTotal = (collectTotalObj instanceof Number) ? ((Number) collectTotalObj).intValue() : 0;
                Object commentTotalObj = sourceAsMap.get(ContentIndex.FIELD_CONTENT_COMMENT_TOTAL);
                Integer commentTotal = (commentTotalObj instanceof Number) ? ((Number) commentTotalObj).intValue() : 0;
                DateTimeFormatter dateTimeStr = DateTimeFormatter.ofPattern(DateConstants.LOCAL_DATE_TIME_PATTERN);
                LocalDateTime createTime = LocalDateTime.parse((String)sourceAsMap.get(ContentIndex.FIELD_CONTENT_CREATE_TIME), dateTimeStr);
                LocalDateTime updateTime = LocalDateTime.parse((String) sourceAsMap.get(ContentIndex.FIELD_CONTENT_UPDATE_TIME), dateTimeStr);
                Integer contentType = ((Number) sourceAsMap.get(ContentIndex.FIELD_CONTENT_TYPE)).intValue();
                String HighLightKeyword = null;
                if ( CollUtil.isNotEmpty(hit.getHighlightFields()) && hit.getHighlightFields().containsKey(ContentIndex.FIELD_CONTENT_TITLE)) {
                    HighLightKeyword = hit.getHighlightFields().get(ContentIndex.FIELD_CONTENT_TITLE).fragments()[0].toString();
                }
                SearchContentResVo searchContentResVo = SearchContentResVo.builder()
                        .contentId(contentId)
                        .cover(cover)
                        .title(title)
                        .highLightTitle(HighLightKeyword)
                        .avatar(avatar)
                        .nickname(nickname)
                        .topic(topic)
                        .type(contentType)
                        .likeTotal(NumberUtil.formatNumberString(likeTotal))
                        .collectTotal(NumberUtil.formatNumberString(collectTotal))
                        .commentTotal(NumberUtil.formatNumberString(commentTotal))
                        .createTime(DateUtil.formatRelativeTime(createTime))
                        .updateTime(DateUtil.formatRelativeTime(updateTime))
                        .build();
                searchContentResVoList.add(searchContentResVo);
            }
        } catch (Exception e) {
            log.error("查询内容失败:", e);
        }
        return PageResponse.success(searchContentResVoList, pageNo, total);
    }

    @Override
    public Response<Long> rebuildDocument(RebuildContentDocReqDTO reqDTO) {
        Long contentId = reqDTO.getContentId();
        List<Map<String, Object>> result = selectMapper.selectEsContentIndexData(contentId, null);
        if (CollectionUtils.isEmpty(result)) throw new BusinessException(ResponseStatusEnum.CONTENT_GET_FAIL);
        for (Map<String, Object> map : result) {
            IndexRequest indexRequest = new IndexRequest(ContentIndex.NAME);
            indexRequest.id(map.get(ContentIndex.FIELD_CONTENT_ID).toString());
            indexRequest.source(map);
            try {
                restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
            } catch (Exception e) {
                log.error("重建文档失败:", e);
            }
        }
        return Response.success();
    }
}
