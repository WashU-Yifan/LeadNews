package com.ethan.behavior.service.impl;

import com.alibaba.fastjson.JSON;
import com.ethan.behavior.service.AppBehaviorService;
import com.heima.common.constants.BehaviorConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.behavior.dtos.LikesBehaviorDto;
import com.heima.model.behavior.dtos.ReadBehaviorDto;
import com.heima.model.behavior.dtos.UnLikesBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.K;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AppBehaviorServiceImpl implements AppBehaviorService {

    @Autowired
    private CacheService cacheService;

    @Override
    public ResponseResult saveLikeBehavior(LikesBehaviorDto dto) {
        //检查参数
        if (dto.getArticleId() == null || dto.getOperation() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        ApUser user = AppThreadLocalUtil.getUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        //判断点赞/取消点赞
        String key = BehaviorConstants.LIKE_BEHAVIOR_KEY + dto.getArticleId().toString();
        if (dto.getOperation() == BehaviorConstants.ARTICLE_LIKE) {
            Object obj = cacheService.hGet(key, user.getId().toString());
            if (obj != null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "已点赞");
            }
            log.info("保存当前key:{}, {}, {}", key, user.getId(), dto);
            cacheService.hPut(key, user.getId().toString(), JSON.toJSONString(dto));
        }
        else {
            log.info("s删除当前key:{}, {}, {}", key, user.getId(), dto);
            cacheService.hDelete(key, user.getId().toString());
        }

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    @Override
    public ResponseResult saveUnLikeBehavior(UnLikesBehaviorDto dto) {
        //检查参数
        if (dto.getArticleId() == null || dto.getType()== null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        ApUser user = AppThreadLocalUtil.getUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        //判断点赞/取消点赞
        String key = BehaviorConstants.UNLIKE_BEHAVIOR_KEY + dto.getArticleId().toString();
        if (dto.getType() == BehaviorConstants.ARTICLE_UNLIKE) {
            Object obj = cacheService.hGet(key, user.getId().toString());
            if (obj != null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "已不喜欢");
            }
            log.info("保存当前key:{}, {}, {}", key, user.getId(), dto);
            cacheService.hPut(key, user.getId().toString(), JSON.toJSONString(dto));
        }
        else {
            log.info("s删除当前key:{}, {}, {}", key, user.getId(), dto);
            cacheService.hDelete(key, user.getId().toString());
        }

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    @Override
    public ResponseResult saveReadBehavior(ReadBehaviorDto dto) {
        if (dto.getArticleId() == null || dto.getCount() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        ApUser user = AppThreadLocalUtil.getUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }


        String key = BehaviorConstants.READ_BEHAVIOR_KEY + dto.getArticleId().toString();

        //如果该用户已阅读过该文章
        ReadBehaviorDto obj = JSON.parseObject((String) cacheService.hGet(key, user.getId().toString()), ReadBehaviorDto.class);
        if (obj != null) {
           dto.setCount(dto.getCount() + obj.getCount());
        }

        log.info("保存当前key:{}, {}, {}", key, user.getId(), dto);
        cacheService.hPut(key, user.getId().toString(), JSON.toJSONString(dto));

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
