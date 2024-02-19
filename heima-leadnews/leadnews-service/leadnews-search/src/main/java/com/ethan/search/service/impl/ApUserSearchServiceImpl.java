package com.ethan.search.service.impl;

import com.ethan.search.pojos.ApUserSearch;
import com.ethan.search.service.ApUserSearchService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.search.dtos.HistorySearchDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class ApUserSearchServiceImpl implements ApUserSearchService {

    @Autowired
    private MongoTemplate mongoTemplate;


    @Override
    public ResponseResult delUserSearchHistory(HistorySearchDto historySearchDto) {
        ApUser apUser = AppThreadLocalUtil.getUser();
        if (apUser == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        if (StringUtils.isEmpty(historySearchDto.getId())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        mongoTemplate.remove(
                Query.query(Criteria.where("userId").is(apUser.getId())
                .and("id").is(historySearchDto.getId())), ApUserSearch.class);

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);

    }

    /**
     * 获取用户搜索记录历史
     * @return
     */
    public ResponseResult findUserSearchHistory() {
        ApUser apUser = AppThreadLocalUtil.getUser();
        if (apUser == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        List<ApUserSearch> apUserSearches = mongoTemplate.find(
                Query.query(Criteria.where("userId").is(apUser.getId()))
                        .with(Sort.by(Sort.Direction.DESC, "createdTime")), ApUserSearch.class);

        return ResponseResult.okResult(apUserSearches);
    }



    /**
     * Save user's search history into MongoDB
     * Mongo will only keep the top 10 recent search records from users.
     * @param keyword
     * @param userId
     */
    @Override
    @Async
    public void saveSearchHistory(String keyword, Integer userId) {
        //whether the search history exist
        Query query = Query.query(Criteria.where("userId").is(userId).and("keyword").is(keyword));
        ApUserSearch apUserSearch = mongoTemplate.findOne(query, ApUserSearch.class);

        //If yes, update the ctime
        if (apUserSearch != null) {
            apUserSearch.setCreatedTime(new Date());
            mongoTemplate.save(apUserSearch);
            return;
        }

        apUserSearch = new ApUserSearch();
        apUserSearch.setUserId(userId);
        apUserSearch.setKeyword(keyword);
        apUserSearch.setCreatedTime(new Date());

        //retrieve the user's search history
        query = Query.query(Criteria.where("userId").is(userId));
        query.with(Sort.by(Sort.Direction.DESC, "createdTime"));
        List<ApUserSearch> apUserSearchList = mongoTemplate.find(query, ApUserSearch.class);

        if (apUserSearchList == null || apUserSearchList.size() < 10) {
            mongoTemplate.save(apUserSearch);
        } else {
            ApUserSearch lastUserSearch = apUserSearchList.get(apUserSearchList.size() - 1);
            mongoTemplate.findAndReplace(Query.query(Criteria.where("id").is(lastUserSearch.getId())), apUserSearch);
        }
    }



}
