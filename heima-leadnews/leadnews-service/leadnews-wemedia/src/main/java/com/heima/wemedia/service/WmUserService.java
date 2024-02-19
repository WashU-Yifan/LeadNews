package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmLoginDto;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.model.wemedia.vos.UserAuthVo;

public interface WmUserService extends IService<WmUser> {

    /**
     * 自媒体端登录
     * @param dto
     * @return
     */
    public ResponseResult login(WmLoginDto dto);


    /**
     * Kafka 消息同步注册自媒体用户
     * @param userAuthVo
     */
    public void syncUser(UserAuthVo userAuthVo);

}