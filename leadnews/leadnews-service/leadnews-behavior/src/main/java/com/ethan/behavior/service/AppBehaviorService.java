package com.ethan.behavior.service;

import com.heima.model.behavior.dtos.LikesBehaviorDto;
import com.heima.model.behavior.dtos.ReadBehaviorDto;
import com.heima.model.behavior.dtos.UnLikesBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;

public interface AppBehaviorService {

    /**
     * 保存用户点赞，阅读行为
     * @param dto
     * @return
     */
    public ResponseResult saveLikeBehavior(LikesBehaviorDto dto);

    public ResponseResult saveUnLikeBehavior(UnLikesBehaviorDto dto);

    public ResponseResult saveReadBehavior(ReadBehaviorDto dto);
}
