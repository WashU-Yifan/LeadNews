package com.heima.user.service;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.AuthPageRequestDto;

import com.heima.model.user.pojos.ApUserRealname;


public interface ApUserAuthService extends IService<ApUserRealname> {

    /**
     * 审核用户列表
     * @param dto
     * @return
     */
    public ResponseResult userList(AuthPageRequestDto dto);

    /**
     * 审核失败
     * @param dto
     * @return
     */
    public ResponseResult authFail(AuthPageRequestDto dto);

    /**
     * 审核通过
     * @param dto
     * @return
     */
    public ResponseResult authSuccess(AuthPageRequestDto dto);
}
