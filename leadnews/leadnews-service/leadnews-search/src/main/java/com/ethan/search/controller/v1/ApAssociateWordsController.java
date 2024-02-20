package com.ethan.search.controller.v1;

import com.ethan.search.service.ApAssociateWordsService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.dtos.UserSearchDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/associate")
public class ApAssociateWordsController {

    @Autowired
    private ApAssociateWordsService apAssociateWordsService;

    @PostMapping("/search")
    public ResponseResult searchAssociate(@RequestBody UserSearchDto userSearchDto) {
        return apAssociateWordsService.searchAssociate(userSearchDto);
    }
}
