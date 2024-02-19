package com.heima.wemedia.listener;


import com.alibaba.fastjson.JSON;
import com.heima.common.constants.ArticleConstants;
import com.heima.common.constants.UserAuthConstants;
import com.heima.common.constants.WemediaConstants;
import com.heima.model.search.vos.SearchArticleVo;
import com.heima.model.wemedia.vos.UserAuthVo;
import com.heima.wemedia.service.WmUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SyncRegisterListener {

    @Autowired
    private WmUserService wmUserService;


    @KafkaListener(topics = UserAuthConstants.WEMEDIA_USER_REGISTER_TOPIC)
    public void onMessage(String message) {
        if (StringUtils.isBlank(message)) {
            log.error("SyncArticleListener, message is blank, topic = {}", UserAuthConstants.WEMEDIA_USER_REGISTER_TOPIC);
            return;
        }
        log.info("SyncArticleListener, message= {}", message);

        UserAuthVo userAuthVo = JSON.parseObject(message, UserAuthVo.class);
        wmUserService.syncUser(userAuthVo);
    }
}
