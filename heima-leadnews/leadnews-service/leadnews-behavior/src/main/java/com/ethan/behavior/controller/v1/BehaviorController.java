package com.ethan.behavior.controller.v1;

import com.ethan.behavior.service.AppBehaviorService;
import com.heima.model.behavior.dtos.LikesBehaviorDto;
import com.heima.model.behavior.dtos.ReadBehaviorDto;
import com.heima.model.behavior.dtos.UnLikesBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class BehaviorController {
    @Autowired
    private AppBehaviorService appBehaviorService;
    @PostMapping("/read_behavior")
    public ResponseResult readBehavior(@RequestBody ReadBehaviorDto dto) {
        return appBehaviorService.saveReadBehavior(dto);
    }

    @PostMapping("/un_likes_behavior")
    public ResponseResult saveUnlikesBehavior(@RequestBody UnLikesBehaviorDto dto) {
        return appBehaviorService.saveUnLikeBehavior(dto);
    }

    @PostMapping("/likes_behavior")
    public ResponseResult saveLikesBehavior(@RequestBody LikesBehaviorDto dto) {
        return appBehaviorService.saveLikeBehavior(dto);
    }
}
