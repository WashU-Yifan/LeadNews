package com.heima.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.constants.BehaviorConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.dtos.LoginDto;
import com.heima.model.user.dtos.UserFollowDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.model.user.pojos.ApUserFan;
import com.heima.model.user.pojos.ApUserFollow;
import com.heima.user.mapper.ApUserFanMapper;
import com.heima.user.mapper.ApUserFollowMapper;
import com.heima.user.mapper.ApUserMapper;
import com.heima.user.service.ApUserService;
import com.heima.utils.common.AppJwtUtil;
import com.heima.utils.thread.AppThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Service
@Transactional
@Slf4j
public class ApUserServiceImpl extends ServiceImpl<ApUserMapper, ApUser> implements ApUserService {

    @Autowired
    private CacheService cacheService;

//    @Autowired
//    private ApUserFanMapper apUserFanMapper;
//
//    @Autowired
//    private ApUserFollowMapper apUserFollowMapper;


    /**
     * app端登录功能
     * @param dto
     * @return
     */
    @Override
    public ResponseResult login(LoginDto dto) {
        //1.正常登录 用户名和密码
        if(StringUtils.isNotBlank(dto.getPhone()) && StringUtils.isNotBlank(dto.getPassword())){
            //1.1 根据手机号查询用户信息
            ApUser dbUser = getOne(Wrappers.<ApUser>lambdaQuery().eq(ApUser::getPhone, dto.getPhone()));
            if(dbUser == null){
                return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST,"用户信息不存在");
            }

            //1.2 比对密码
            String salt = dbUser.getSalt();
            String password = dto.getPassword();
            String pswd = DigestUtils.md5DigestAsHex((password + salt).getBytes());
            if(!pswd.equals(dbUser.getPassword())){
                return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
            }

            //1.3 返回数据  jwt  user
            String token = AppJwtUtil.getToken(dbUser.getId().longValue());
            Map<String,Object> map = new HashMap<>();
            map.put("token",token);
            dbUser.setSalt("");
            dbUser.setPassword("");
            map.put("user",dbUser);

            return ResponseResult.okResult(map);
        }else {
            //2.游客登录
            Map<String,Object> map = new HashMap<>();
            map.put("token",AppJwtUtil.getToken(0L));
            return ResponseResult.okResult(map);
        }


    }


    @Override
    public ResponseResult follow(UserFollowDto dto) {
        //参数检查
        if (dto.getArticleId() == null || dto.getAuthorId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        Integer userId = AppThreadLocalUtil.getUser().getId();
        if (userId == dto.getAuthorId()) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "不可以关注自己");
        }

        ApUser user = getById(userId);
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        /**
         * todo: 保存于mysql中
         */
        ApUserFollow apUserFollow = new ApUserFollow();
        apUserFollow.setFollowId(dto.getAuthorId());
        apUserFollow.setUserId(user.getId());
        apUserFollow.setCreatedTime(new Date());

        ApUserFan apUserFan = new ApUserFan();
        apUserFan.setUserId(dto.getAuthorId());
        apUserFan.setFansId(user.getId());
        apUserFan.setFansName(user.getName());
        apUserFan.setCreatedTime(new Date());

        //关注操作
        if (dto.getOperation() == BehaviorConstants.USER_FOLLOW) {
            //检查是否关注/粉丝关系已存在
            if (cacheService.hGet(BehaviorConstants.FOLLOW_BEHAVIOR_KEY + user.getId(), dto.getAuthorId().toString()) != null
                || cacheService.hGet(BehaviorConstants.FOLLOW_BEHAVIOR_KEY + user.getId(), dto.getAuthorId().toString()) != null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.DATA_EXIST, "不可重复关注");
            }

            cacheService.hPut(BehaviorConstants.FAN_BEHAVIOR_KEY + dto.getAuthorId(), user.getId().toString(), JSON.toJSONString(apUserFan));
            cacheService.hPut(BehaviorConstants.FOLLOW_BEHAVIOR_KEY + user.getId(), dto.getAuthorId().toString(), JSON.toJSONString(apUserFollow));

        }
        else {
            //取消关注
            cacheService.hDelete(BehaviorConstants.FAN_BEHAVIOR_KEY + dto.getAuthorId(), user.getId().toString());
            cacheService.hDelete(BehaviorConstants.FOLLOW_BEHAVIOR_KEY + user.getId(), dto.getAuthorId().toString());

        }

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
