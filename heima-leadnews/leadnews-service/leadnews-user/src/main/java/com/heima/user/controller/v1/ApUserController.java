package com.heima.user.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.UserFollowDto;
import com.heima.user.service.ApUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/user")
@RestController
public class ApUserController {
    @Autowired
    private ApUserService apUserService;
    @PostMapping("/user_follow")
    public ResponseResult follow(@RequestBody UserFollowDto dto) {
        return apUserService.follow(dto);
    }
}
