package com.taoxin.communitysharing.search.business.config.canal;

import com.alibaba.nacos.shaded.com.google.common.collect.Maps;
import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.taoxin.communitysharing.common.Enums.StatusEnum;
import com.taoxin.communitysharing.search.business.config.ElasticsearchRestHighLevelClient;
import com.taoxin.communitysharing.search.business.domain.mapper.SelectMapper;
import com.taoxin.communitysharing.search.business.enums.ContentStatusEnum;
import com.taoxin.communitysharing.search.business.enums.ContentVisibaleEnum;
import com.taoxin.communitysharing.search.business.index.ContentIndex;
import com.taoxin.communitysharing.search.business.index.UserIndex;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class CanalSchedule implements Runnable{
    @Resource
    private CanalProperties canalProperties;
    @Resource
    private CanalConnector canalConnector;
    @Resource
    private SelectMapper selectMapper;
    @Resource
    private ElasticsearchRestHighLevelClient elasticsearchRestHighLevelClient;

    @Override
    @Scheduled(fixedDelay = 100) // 100ms执行一次
    public void run() {
        // 初始化批次 ID，-1 表示未开始或未获取到数据
        long batchId = -1;
        try {
            // 获取批量消息，返回的数据量由 batchSize 控制，若不足，则拉取已有的
            Message message = canalConnector.getWithoutAck(canalProperties.getBatchSize());
            batchId = message.getId(); // 获取批量ID
            // 获取当前批次中的数据条数
            long size = message.getEntries().size();
            if (batchId == -1 || size == 0) {
                // 没抓到，可能太快，先等1s
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {}
            } else {
                printEntry(message.getEntries());
            }
            canalConnector.ack(batchId);
        } catch (Exception e) {
            log.error("Canal调度失败: {}", e);
            canalConnector.rollback(batchId);
        }
    }

    /**
     * 打印数据(官方文档示例)
     * @param entrys
     */
    private void printEntry(List<CanalEntry.Entry> entrys) throws Exception {
        for (CanalEntry.Entry entry : entrys) {
            if (entry.getEntryType() == CanalEntry.EntryType.ROWDATA) { // 如果是数据行
                // 获取事件类型（如：INSERT、UPDATE、DELETE 等等）
                CanalEntry.EventType eventType = entry.getHeader().getEventType();
                // 获取数据库名称
                String database = entry.getHeader().getSchemaName();
                // 获取表名称
                String table = entry.getHeader().getTableName();
                // 解析出 RowChange 对象，包含 RowData 和事件相关信息
                CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
                // 获取所有行
                for (CanalEntry.RowData rowData : rowChange.getRowDatasList()) {
                    // 获取行中所有列的最新值（AfterColumns）
                    List<CanalEntry.Column> columns = rowData.getAfterColumnsList();
                    // 将列数据解析为 Map，方便后续处理
                    Map<String, Object> columnMap = parseColumns2Map(columns);
                    // todo: 列数据处理
                    log.info("数据库: {}, 表: {}, 事件类型: {}, 列数据: {}", database, table, eventType, columnMap);
                    processEvent(columnMap, table, eventType);
                }
            }
        }
    }

    /**
     * 处理事件
     * @param columnMap 列数据
     * @param table 表名
     * @param eventType 事件类型
     */
    private void processEvent(Map<String, Object> columnMap, String table, CanalEntry.EventType eventType) throws Exception {
        switch (table) {
            case "t_content" -> handlerContent(columnMap, eventType);
            case "t_user" -> handlerUser(columnMap, eventType);
            default -> log.info("未知表: {}", table);
        }
    }


    /**
     * 处理内容表
     * @param columnMap 列数据
     * @param eventType 事件类型
     */
    private void handlerContent(Map<String, Object> columnMap, CanalEntry.EventType eventType) throws Exception {
        Long contentId = Long.parseLong(columnMap.get("id").toString());
        switch (eventType) {
            case INSERT -> syncContentIndex(contentId);
            case UPDATE -> {
                // 变更后的状态
                Integer status = Integer.parseInt(columnMap.get("status").toString());
                // 可见性
                Integer visible = Integer.parseInt(columnMap.get("visible").toString());
                log.info("更新内容==> status: {}, visible: {}", status, visible);
                if (Objects.equals(status, ContentStatusEnum.NORMAL.getCode()) && Objects.equals(visible, ContentVisibaleEnum.PUBLIC.getCode())) {
                    // 只有正常状态且可见的内容才同步
                    log.info("同步内容索引: {}", contentId);
                    syncContentIndex(contentId);
                }else if (
                        Objects.equals(visible, ContentVisibaleEnum.PRIVATE.getCode())
                        || Objects.equals(status, ContentStatusEnum.DELETED.getCode())
                        || Objects.equals(status, ContentStatusEnum.DOWN.getCode())
                ) {
                    // todo 如果私密、删除、下架的内容，则删除索引
                    deleteContentIndex(contentId);
                }
            }
            default -> log.warn("未知事件: {}", eventType);
        }
    }

    private void deleteContentIndex(Long contentId) throws Exception {
        // 创建删除请求
        DeleteRequest deleteRequest = new DeleteRequest(ContentIndex.NAME, String.valueOf(contentId));
        // 删除
        elasticsearchRestHighLevelClient.restHighLevelClient().delete(deleteRequest, RequestOptions.DEFAULT);
    }

    /**
     * 同步内容索引
     * @param contentId 内容ID
     */
    private void syncContentIndex(Long contentId) throws Exception {
        List<Map<String, Object>> content = selectMapper.selectEsContentIndexData(contentId,null); // list(字段 值)
        if (CollectionUtils.isEmpty(content)) {
            log.warn("未查询到需要同步的数据，contentId={}", contentId);
            return;
        }
        for (Map<String, Object> field : content) {
            // 实例化索引对象
            IndexRequest indexRequest = new IndexRequest(ContentIndex.NAME);
            // 设置文档的 ID，使用记录中的主键 “id” 字段值
            indexRequest.id(String.valueOf(field.get(ContentIndex.FIELD_CONTENT_ID)));
            indexRequest.source(field); // 设置文档内容
            // 索引
            elasticsearchRestHighLevelClient.restHighLevelClient().index(indexRequest, RequestOptions.DEFAULT);
        }
    }

    /**
     * 处理用户表
     * @param columnMap 列数据
     * @param eventType 事件类型
     */
    private void handlerUser(Map<String, Object> columnMap, CanalEntry.EventType eventType) throws Exception {
        Long userId = Long.parseLong(columnMap.get("id").toString());
        switch (eventType) {
            case INSERT -> syncUserIndex(userId);
            case UPDATE -> {
                // todo 更新用户
                Integer status = Integer.parseInt(columnMap.get("status").toString());
                Integer isDelete = Integer.parseInt(columnMap.get("is_deleted").toString());

                if (Objects.equals(status, StatusEnum.ENABLED.getCode()) && Objects.equals(isDelete, 0)) {
                    // 只有账号启用且未删除的才更新
                    syncContentIndexAndUserIndex(userId);
                } else if (Objects.equals(status, StatusEnum.DISABLED.getCode()) || Objects.equals(isDelete, 1)){
                    // 删除用户索引
                    deleteUserIndex(String.valueOf(userId));
                }
            }
            default -> log.warn("未知事件: {}", eventType);
        }
    }

    private void deleteUserIndex(String userId) throws Exception {
        DeleteRequest deleteRequest = new DeleteRequest(UserIndex.NAME, userId);
        elasticsearchRestHighLevelClient.restHighLevelClient().delete(deleteRequest, RequestOptions.DEFAULT);
    }

    /**
     * 同步用户索引和内容索引
     * @param userId 用户ID
     * @throws Exception 抛出异常
     */
    private void syncContentIndexAndUserIndex(Long userId) throws Exception{
        // 创建一个 BulkRequest
        BulkRequest bulkRequest = new BulkRequest();

        // 1. 用户索引
        List<Map<String, Object>> userResult = selectMapper.selectEsUserIndexData(userId);

        for (Map<String, Object> field : userResult) {
            // 创建索引请求对象，指定索引名称
            IndexRequest indexRequest = new IndexRequest(UserIndex.NAME);
            indexRequest.id(String.valueOf(field.get(UserIndex.FIELD_USER_ID)));
            indexRequest.source(field);
            bulkRequest.add(indexRequest);
        }

        // 2. 内容索引
        List<Map<String, Object>> contentResult = selectMapper.selectEsContentIndexData(null, userId);

        for (Map<String, Object> field : contentResult) {
            // 创建索引请求对象，指定索引名称
            IndexRequest indexRequest = new IndexRequest(ContentIndex.NAME);
            indexRequest.id(String.valueOf(field.get(ContentIndex.FIELD_CONTENT_ID)));
            indexRequest.source(field);
            bulkRequest.add(indexRequest);
        }
        elasticsearchRestHighLevelClient.restHighLevelClient().bulk(bulkRequest, RequestOptions.DEFAULT);
    }

    /**
     * 同步用户索引
     * @param userId 用户ID
     * @throws Exception 抛出异常
     */
    private void syncUserIndex(Long userId) throws Exception {
        // 同步用户索引
        List<Map<String, Object>> userResult = selectMapper.selectEsUserIndexData(userId);

        for (Map<String, Object> field : userResult) {
            // 创建索引对象
            IndexRequest indexRequest = new IndexRequest(UserIndex.NAME);
            // 设置文档的 ID
            indexRequest.id(String.valueOf(field.get(UserIndex.FIELD_USER_ID)));
            // 设置文档内容
            indexRequest.source(field);
            // 索引
            elasticsearchRestHighLevelClient.restHighLevelClient().index(indexRequest, RequestOptions.DEFAULT);
        }
    }

    /**
     * 解析列数据
     * @param columns 列数据
     * @return  Map
     */
    private static Map<String, Object> parseColumns2Map(List<CanalEntry.Column> columns) {
        Map<String, Object> map = Maps.newHashMap();
        columns.forEach(column -> {
            if (Objects.isNull(column)) return;
            map.put(column.getName(), column.getValue());
        });
        return map;
    }
}
