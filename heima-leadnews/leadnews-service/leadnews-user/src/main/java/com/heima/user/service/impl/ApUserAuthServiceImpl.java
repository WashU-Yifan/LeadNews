package com.heima.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.constants.UserAuthConstants;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.search.vos.SearchArticleVo;
import com.heima.model.user.dtos.AuthPageRequestDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.model.user.pojos.ApUserRealname;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.model.wemedia.vos.UserAuthVo;
import com.heima.user.mapper.ApUserAuthMapper;
import com.heima.user.mapper.ApUserMapper;
import com.heima.user.service.ApUserAuthService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class ApUserAuthServiceImpl extends ServiceImpl<ApUserAuthMapper, ApUserRealname> implements ApUserAuthService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ApUserMapper apUserMapper;
    /**
     * 审核用户列表
     * @param dto
     * @return
     */
    public ResponseResult userList(AuthPageRequestDto dto) {
        dto.checkParam();
        //设置查询条件
        IPage page = new Page(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<ApUserRealname> queryWrapper = Wrappers.<ApUserRealname>lambdaQuery();

        if (dto.getStatus() != null) {
            queryWrapper.eq(ApUserRealname::getStatus, dto.getStatus());
        }
        //分页
        page = page(page, queryWrapper);
        ResponseResult responseResult = new PageResponseResult(dto.getPage(), dto.getSize(), (int) page.getTotal());
        responseResult.setData(page.getRecords());

        return responseResult;

    }

    /**
     * 审核失败
     * @param dto
     * @return
     */
    public ResponseResult authFail(AuthPageRequestDto dto) {
        if (dto.getId() == null || StringUtils.isNotEmpty(dto.getMsg())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //以通过审核用户不可以驳回
        if (dto.getStatus() == UserAuthConstants.AUTH_TYPE_SUCCESS) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        ApUserRealname apUserRealname = getById(dto.getId());

        if(apUserRealname == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        //更新状态
        apUserRealname.setStatus(UserAuthConstants.AUTH_TYPE_FAIL);
        apUserRealname.setUpdatedTime(new Date());

        updateById(apUserRealname);

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 审核通过
     * @param dto
     * @return
     */
    public ResponseResult authSuccess(AuthPageRequestDto dto) {
        if (dto.getId() == null || StringUtils.isNotEmpty(dto.getMsg())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        ApUserRealname apUserRealname = getById(dto.getId());

        if(apUserRealname == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        //更新状态
        apUserRealname.setStatus(UserAuthConstants.AUTH_TYPE_SUCCESS);
        apUserRealname.setUpdatedTime(new Date());
        updateById(apUserRealname);

        ApUser apUser = apUserMapper.selectById(apUserRealname.getUserId());
        apUser.setIdentityAuthentication(true);
        apUserMapper.updateById(apUser);

        registerWmUser(apUserRealname, apUser);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }


    public void registerWmUser(ApUserRealname apUserRealname, ApUser apUser) {
        UserAuthVo vo = new UserAuthVo();
        BeanUtils.copyProperties(apUser, vo);
        vo.setNickname(apUser.getName());

        BeanUtils.copyProperties(apUserRealname, vo);

        kafkaTemplate.send(UserAuthConstants.WEMEDIA_USER_REGISTER_TOPIC, JSON.toJSONString(vo));
    }

}
