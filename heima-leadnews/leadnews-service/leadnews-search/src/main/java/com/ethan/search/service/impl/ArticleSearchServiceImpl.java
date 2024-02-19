package com.ethan.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.ethan.search.service.ApUserSearchService;
import com.ethan.search.service.ArticleSearchService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.search.dtos.UserSearchDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ArticleSearchServiceImpl implements ArticleSearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private ApUserSearchService apUserSearchService;

    /**
     * es 文章分页检查
     * @param userSearchDto
     * @return
     */
    public ResponseResult search(UserSearchDto userSearchDto) {

        if (userSearchDto == null || StringUtils.isBlank(userSearchDto.getSearchWords())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        //add to the search record history
        ApUser apUser = AppThreadLocalUtil.getUser();
        if (apUser != null && userSearchDto.getFromIndex() == 0) {
            apUserSearchService.saveSearchHistory(userSearchDto.getSearchWords(), apUser.getId());
        }
        //Set up the search qeury
        SearchRequest searchRequest = setSearchQuery(userSearchDto);

        List<Map> list = new ArrayList<>();
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            SearchHit[] hits = searchResponse.getHits().getHits();
            for (SearchHit hit : hits) {
                String json = hit.getSourceAsString();
                Map map = JSON.parseObject(json, Map.class);
                //处理高亮
                if (hit.getHighlightFields() != null && hit.getHighlightFields().size() > 0) {
                    Text[] titles = hit.getHighlightFields().get("title").getFragments();
                    String title = StringUtils.join(titles);
                    map.put("h_title", title);
                }
                else {
                    map.put("h_title", map.get("title"));
                }
                list.add(map);
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR, "ES 消息处理错误");
        }

        return ResponseResult.okResult(list);
    }

    SearchRequest setSearchQuery(UserSearchDto userSearchDto) {
        SearchRequest searchRequest = new SearchRequest("app_info_article");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //布尔查询
        BoolQueryBuilder boolQueryBuilder = setSearchConstraint(userSearchDto);
        //高亮查询
        HighlightBuilder highlightBuilder = setSearchHighlight();

        //分页
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(userSearchDto.getPageSize());

        //按照发布时间倒序查询
        searchSourceBuilder.sort("publishTime", SortOrder.DESC);

        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.highlighter(highlightBuilder);
        searchRequest.source(searchSourceBuilder);
        return searchRequest;
    }

    BoolQueryBuilder setSearchConstraint(UserSearchDto userSearchDto) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        //关键字的分次之后查询
        QueryStringQueryBuilder queryStringQueryBuilder = QueryBuilders.queryStringQuery(userSearchDto.getSearchWords())
                .field("title")
                .field("content")
                .defaultOperator(Operator.OR);
        boolQueryBuilder.must(queryStringQueryBuilder);

        //查询小于mindate的数据
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("publishTime").lt(userSearchDto.getMinBehotTime().getTime());
        boolQueryBuilder.filter(rangeQueryBuilder);
        return boolQueryBuilder;
    }

    HighlightBuilder setSearchHighlight() {

        //设置高亮，title
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.preTags("<font style = 'color: red; font-size: inherit;'>");
        highlightBuilder.postTags("</font>");
        return highlightBuilder;
    }

}
