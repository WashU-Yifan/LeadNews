package com.ethan.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.admin.dtos.AdLoginDto;
import com.heima.model.admin.pojos.AdUser;
import com.heima.model.common.dtos.ResponseResult;



public interface AdLoginService extends IService<AdUser> {

    /**
     * 管理员端登录功能
     * @param loginDto
     * @return
     */
    public ResponseResult login(AdLoginDto loginDto);
}
