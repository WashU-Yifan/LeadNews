package com.heima.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import com.alibaba.fastjson.JSON;
import com.heima.es.mapper.ApArticleMapper;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ApArticleTest {


    @Autowired
    private ApArticleMapper apArticleMapper;

    @Autowired
    private ElasticsearchClient client;

    //@Test
    public void existIndex() {
        try {
            BooleanResponse booleanResponse = client.indices().exists(e -> e.index("索引"));
            System.out.println(booleanResponse);
        } catch (IOException e) {
            System.out.println("向es中检测索引 索引 出错，错误信息为：{}" + e.getMessage());
        }

    }

    //@Test
    public void findApplogs(String indexName, String filedName, String filedValue) {
        try {
            SearchResponse<Map> searchResponse = client.search(s -> s.index("app_info_article")
                            .query(q -> q
                                    .match(t -> t
                                            .field("title")
                                            .query("索引")
                                    )),
                    Map.class);
            List<Hit<Map>> hitList = searchResponse.hits().hits();
            List<Map> appLogList = new ArrayList<>();
            for (Hit<Map> mapHit : hitList) {
                appLogList.add(mapHit.source());
            }
            System.out.println(appLogList);
        } catch (IOException e) {
            System.out.println("向es中查询文章 索引 出错，错误信息为：{}" + e.getMessage());
        }

    }
}




