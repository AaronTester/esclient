package com.aaron.esclient;

import com.aaron.esclient.pojo.User;
import com.alibaba.fastjson.JSON;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.PutIndexTemplateRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class EsclientApplicationTests {

    @Autowired
    public RestHighLevelClient restHighLevelClient;

    //创建索引
    @Test
    void contextLoads() throws IOException {
        CreateIndexRequest request = new CreateIndexRequest("es_index");
        CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
        System.out.println(createIndexResponse);
    }

    //删除索引
    @Test
    public void deleteIndex() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest("es_index");
        AcknowledgedResponse delete = restHighLevelClient.indices().delete(request, RequestOptions.DEFAULT);
        boolean isSuccessful = delete.isAcknowledged();
        System.out.println(isSuccessful);
    }

    //返回索引是否存在
    @Test
    public void existIndex() throws IOException {
        GetIndexRequest request = new GetIndexRequest("es_index");
        boolean exists = restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);

        System.out.println(exists);
    }

    @Test
    public void addDocument() throws IOException {
        List<String> list = new ArrayList<>();
        list.add(UUID.randomUUID().toString());
        // 创建文档对象
        User user = new User("彼岸舞111", list);

        // 指定索引库
        IndexRequest indexRequest = new IndexRequest("es_index");

        // 设置参数 id 超时时间 和数据源
        indexRequest.id("2").timeout(TimeValue.timeValueSeconds(5)).source(JSON.toJSONString(user), XContentType.JSON);

        // 执行请求
        IndexResponse index = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);

        System.out.println(index.toString());

        System.out.println(index.status());
    }

    //删除文档
    @Test
    public void deleteDocument() throws IOException {
        DeleteRequest request = new DeleteRequest("es_index", "2");
        request.timeout("1s");
        DeleteResponse deleteResponse = restHighLevelClient.delete(request, RequestOptions.DEFAULT);

        System.out.println(deleteResponse.status());
    }


    //查询
    @Test
    public void search() throws IOException {
        SearchRequest request = new SearchRequest("es_index");

        //构建搜索条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        // SearchRequest 搜索请求
        // SearchSourceBuilder 条件构造
        // HighlightBuilder 构建高亮
        // TermQueryBuilder 精确查询
        // MatchAllQueryBuilder .....
        MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
        sourceBuilder.query(matchAllQueryBuilder)
                .timeout(new TimeValue(60, TimeUnit.SECONDS));

        request.source(sourceBuilder);

        SearchResponse searchResponse = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        System.out.println(JSON.toJSONString(searchResponse.getHits(), true));
        System.out.println("===================================");
        for (SearchHit documentFields : searchResponse.getHits()) {
            System.out.println(documentFields.getSourceAsMap());
        }

    }

    //批量插入数据
    @Test
    public void BulkRequest() throws IOException {

        BulkRequest bulkRequest = new BulkRequest()
                .timeout("5s");

        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
        list.add("3");

        List<User> users = Arrays.asList(new User("dai1", list), new User("dai2", list), new User("dai3", list));

        for (User user : users) {
            bulkRequest.add(new IndexRequest("es_index")
                    //.id("xxx")
                    .source(JSON.toJSONString(user), XContentType.JSON));
        }

        BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        //是否失败,false表示成功
        System.out.println(bulkResponse.hasFailures());
        System.out.println(bulkResponse.status());
    }


    //更新文档
    @Test
    public void updateDocument() throws IOException {
        UpdateRequest request = new UpdateRequest("es_index", "4");
        request.timeout("1s");

        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
        list.add("3");

        User user = new User("dai22", list);
        request.doc(JSON.toJSONString(user), XContentType.JSON);
        UpdateResponse updateResponse = restHighLevelClient.update(request, RequestOptions.DEFAULT);

        System.out.println(updateResponse.status());
    }

}
