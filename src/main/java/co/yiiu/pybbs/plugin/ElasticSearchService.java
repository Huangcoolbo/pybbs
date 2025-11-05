package co.yiiu.pybbs.plugin;

import co.yiiu.pybbs.config.service.BaseService;
import co.yiiu.pybbs.model.SystemConfig;
import co.yiiu.pybbs.service.ISystemConfigService;
import co.yiiu.pybbs.util.MyPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by tomoya.
 * Copyright (c) 2018, All Rights Reserved.
 * https://atjiu.github.io
 */
@Component
@DependsOn("mybatisPlusConfig")
public class ElasticSearchService implements BaseService<RestHighLevelClient> {

    @Resource
    private ISystemConfigService systemConfigService;
    private final Logger log = LoggerFactory.getLogger(ElasticSearchService.class);
    private RestHighLevelClient client;
    // 索引名
    private String name;

    public static XContentBuilder topicMappingBuilder;
    public static XContentBuilder messageMappingBuilder;

    static {
        try {
            topicMappingBuilder = JsonXContent.contentBuilder()
                    .startObject()
                    .startObject("properties")
                    // .startObject("id")
                    // .field("type", "integer")
                    // .endObject()
                    .startObject("title")
                    .field("type", "text")
                    .field("analyzer", "ik_max_word")
                    .field("index", "true")
                    .endObject()
                    .startObject("content")
                    .field("type", "text")
                    .field("analyzer", "ik_max_word")
                    .field("index", "true")
                    .endObject()
                    .endObject()
                    .endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            messageMappingBuilder = JsonXContent.contentBuilder()
                    .startObject()
                    .startObject("properties")
                    .startObject(("dialogId"))
                    .field("type", "integer")
                    .field("index", "true")
                    .endObject()
                    .startObject("message")
                    .field("type", "text")
                    .field("analyzer", "ik_max_word")
                    .field("search_analyzer", "ik_smart")
                    .field("index", "true")
                    .endObject()
                    .endObject()
                    .endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public RestHighLevelClient instance() {
        if (this.client != null) return client;
        try {
            SystemConfig systemConfigHost = systemConfigService.selectByKey("elasticsearch_host");
            String host = systemConfigHost.getValue();
            SystemConfig systemConfigPort = systemConfigService.selectByKey("elasticsearch_port");
            String port = systemConfigPort.getValue();
            SystemConfig systemConfigindex = systemConfigService.selectByKey("elasticsearch_index");
            // index = systemConfigindex.getValue();

            if (StringUtils.isEmpty(host) || StringUtils.isEmpty(port)) return null;
            client = new RestHighLevelClient(RestClient.builder(new HttpHost(host, Integer.parseInt(port), "http")));
            // 判断索引是否存在，不存在创建
            if (!this.existIndex("topic")){
                this.createIndex("topic","topic", topicMappingBuilder);
            }
            if (!this.existIndex("message")){
                this.createIndex("message","message", messageMappingBuilder);
            }
            return client;
        } catch (NumberFormatException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    // 创建索引 PUT http://localhost:9200/topic
    public boolean createIndex(String index, String type, XContentBuilder mappingBuilder) {
        try {
            if (this.instance() == null) return false;
            // 创建索引的请求（index为索引名）
            CreateIndexRequest request = new CreateIndexRequest(index);
            request.settings(Settings.builder().put("index.number_of_shards", 1).put("index.number_of_replicas", 1));
            // 相当于创建表结构的语句
            if (mappingBuilder != null) request.mapping(type, mappingBuilder);
            // 向 ES 发送一个 创建索引请求。
            CreateIndexResponse response = this.client.indices().create(request, RequestOptions.DEFAULT);
            return response.isAcknowledged();
        } catch (IOException e) {
            log.error(e.getMessage());
            return false;
        }
    }

    // 检查索引是否存在
    public boolean existIndex(String index) {
        try {
            if (this.instance() == null) return false;
            GetIndexRequest request = new GetIndexRequest();
            // request.indices(index);
            request.indices(index);
            request.local(false);
            request.humanReadable(true);
            return client.indices().exists(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error(e.getMessage());
            return false;
        }
    }

    // 删除索引
    public boolean deleteIndex(String index) {
        try {
            if (this.instance() == null) return false;
            DeleteIndexRequest request = new DeleteIndexRequest(index);
            request.indicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);
            AcknowledgedResponse response = client.indices().delete(request, RequestOptions.DEFAULT);
            return response.isAcknowledged();
        } catch (IOException e) {
            log.error(e.getMessage());
            return false;
        }
    }
    // 消息的会话号也放在字段里，对应message表里的dialogId字段
    // 创建文档
    public void createDocument(String index, String type, String id, Map<String, Object> source) {
        try {
            if (this.instance() == null) return;
            IndexRequest request = new IndexRequest(index, type, id);
            // 新增文档数据
            request.source(source);
            // 执行请求
            client.index(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    // 更新文档
    public void updateDocument(String index, String type, String id, Map<String, Object> source) {
        try {
            if (this.instance() == null) return;
            UpdateRequest request = new UpdateRequest(index, type, id);
            request.doc(source);
            client.update(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    // 删除文档
    public void deleteDocument(String index, String type, String id) {
        try {
            if (this.instance() == null) return;
            DeleteRequest request = new DeleteRequest(index, type, id);
            client.delete(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    // 批量创建文档
    public void bulkDocument(String index, String type, Map<String, Map<String, Object>> sources) {
        try {
            if (this.instance() == null) return;
            BulkRequest requests = new BulkRequest();
            Iterator<String> it = sources.keySet().iterator();
            int count = 0;
            while (it.hasNext()) {
                count++;
                String next = it.next();
                IndexRequest request = new IndexRequest(index, type, next);
                request.source(sources.get(next));
                requests.add(request);
                if (count % 1000 == 0) {
                    client.bulk(requests, RequestOptions.DEFAULT);
                    requests.requests().clear();
                    count = 0;
                }
            }
            if (requests.numberOfActions() > 0) client.bulk(requests, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    // 批量删除文档
    public void bulkDeleteDocument(String index, String type, List<Integer> ids) {
        try {
            if (this.instance() == null) return;
            BulkRequest requests = new BulkRequest();
            int count = 0;
            for (Integer id : ids) {
                count++;
                DeleteRequest request = new DeleteRequest(index, type, String.valueOf(id));
                requests.add(request);
                if (count % 1000 == 0) {
                    client.bulk(requests, RequestOptions.DEFAULT);
                    requests.requests().clear();
                    count = 0;
                }
            }
            if (requests.numberOfActions() > 0) client.bulk(requests, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 查询
     *
     * @param pageNo
     * @param pageSize
     * @param keyword  要查询的内容
     * @param fields   要查询的字段，可以为多个 这里为"title","content"
     * @return 分页对象 {@link Page}
     */
    public MyPage<Map<String, Object>> searchDocument(String index, Integer dialogId, Integer pageNo, Integer pageSize, String keyword, String... fields) {
        try {
            if (this.instance() == null) return new MyPage<>();
            SearchRequest request = new SearchRequest(index);
            SearchSourceBuilder builder = new SearchSourceBuilder();
            // “在 title 和 content 两个字段里查包含 keyword 的文档
            BoolQueryBuilder bool = QueryBuilders.boolQuery()
                    .must(QueryBuilders.multiMatchQuery(keyword, fields)); // 原来的关键词多字段匹配
            if (dialogId != null && dialogId > 0) {
                bool.filter(QueryBuilders.termQuery("dialogId", dialogId));
            }
            builder.query(bool);

            builder.from((pageNo - 1) * pageSize).size(pageSize);
            request.source(builder);
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            // 总条数
            long totalCount = response.getHits().getTotalHits();
            // 结果集
            List<Map<String, Object>> records = Arrays.stream(response.getHits().getHits()).map(hit -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", hit.getId());
                map.putAll(hit.getSourceAsMap());
                return map;
            }).collect(Collectors.toList());
            MyPage<Map<String, Object>> page = new MyPage<>(pageNo, pageSize);
            page.setTotal(totalCount);
            page.setRecords(records);
            return page;
        } catch (IOException e) {
            log.error(e.getMessage());
            return new MyPage<>();
        }
    }
}
