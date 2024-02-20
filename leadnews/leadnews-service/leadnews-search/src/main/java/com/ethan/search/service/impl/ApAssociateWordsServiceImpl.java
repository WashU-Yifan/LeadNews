package com.ethan.search.service.impl;

import com.ethan.search.pojos.ApAssociateWords;
import com.ethan.search.service.ApAssociateWordsService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.search.dtos.UserSearchDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ApAssociateWordsServiceImpl implements ApAssociateWordsService {


    @Autowired
    private MongoTemplate mongoTemplate;
    /**
     *
     * @param userSearchDto
     * @return
     */
    @Override
    public ResponseResult searchAssociate(UserSearchDto userSearchDto) {
        if (StringUtils.isEmpty(userSearchDto.getSearchWords())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        if (userSearchDto.getPageSize() > 20) {
            userSearchDto.setPageSize(20);
        }

        Query regexQuery = Query.query(Criteria.where("associateWords").regex(".*?\\" + userSearchDto.getSearchWords() + ".*"));
        regexQuery.limit(userSearchDto.getPageSize());
        List<ApAssociateWords> apAssociateWords = mongoTemplate.find(regexQuery, ApAssociateWords.class);

        return ResponseResult.okResult(apAssociateWords);
    }
}
