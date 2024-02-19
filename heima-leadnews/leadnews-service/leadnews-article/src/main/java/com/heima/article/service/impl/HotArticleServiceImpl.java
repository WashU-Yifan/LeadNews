package com.heima.article.service.impl;

import com.alibaba.fastjson.JSON;
import com.ethan.apis.wemedia.IWemediaClient;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.HotArticleService;
import com.heima.common.constants.ArticleConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.vos.HotArticleVo;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.pojos.WmChannel;
import lombok.extern.slf4j.Slf4j;

import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class HotArticleServiceImpl implements HotArticleService {

    @Autowired
    private ApArticleMapper apArticleMapper;

    @Autowired
    private IWemediaClient wemediaClient;

    @Autowired
    private CacheService cacheService;

    /**
     * 计算热点文章
     */
    @Override
    public void cacheHotArticle() {

        //1 查询最近五天文章
        Date date = DateTime.now().minusDays(5).toDate();
        List<ApArticle> articleList = apArticleMapper.findArticleListByLast5days(date);


        //2 计算分值
        List<HotArticleVo> hotArticleVos = computeHotArticle(articleList);

        //3 根据频道保存至Redis中
        ResponseResult response = wemediaClient.getChannels();

        if (response.getCode() == AppHttpCodeEnum.SUCCESS.getCode()) {
            List<WmChannel> wmChannels = JSON.parseArray(JSON.toJSONString(response.getData()), WmChannel.class);
            cacheHotArticleToRedis(hotArticleVos, wmChannels);
        }
    }

    /**
     * 根据频道缓存热点文章
     * @param hotArticleVos
     * @param channels
     */
    private void cacheHotArticleToRedis(List<HotArticleVo> hotArticleVos, List<WmChannel> channels) {

        for (WmChannel channel : channels) {
            if (channel.getId() == null) continue;
            //获取当前频道下的文章及分值
            List<HotArticleVo> vos = hotArticleVos.stream().filter(vo -> channel.getId().equals(vo.getChannelId())).collect(Collectors.toList());
            //频道页热点文章
            sortCacheArticle(vos, ArticleConstants.HOT_ARTICLE_FIRST_PAGE + channel.getId());
        }

        //推荐页热点文章
        sortCacheArticle(hotArticleVos, ArticleConstants.HOT_ARTICLE_FIRST_PAGE + ArticleConstants.DEFAULT_TAG);
    }

    /**
     * 根据分值排序并保存
     * @param vos
     * @param key
     */
    private void sortCacheArticle(List<HotArticleVo> vos, String key) {
        //维护一个大小为30的最小堆
        PriorityQueue<HotArticleVo> pq = new PriorityQueue<>(Comparator.comparing(HotArticleVo::getScore));
        for (HotArticleVo vo : vos) {
            pq.add(vo);
            if (pq.size() > ArticleConstants.HOT_ARTICLE_MAX_SIZE) {
                pq.remove();
            }
        }

        List<HotArticleVo> topArticles = pq.stream().collect(Collectors.toList());
        //保证有序性
        topArticles.sort(Comparator.comparing(HotArticleVo::getScore).reversed());
        cacheService.set(key, JSON.toJSONString(topArticles));
    }

    /**
     * 返回带分值的文章数据
     * @param articleList
     * @return
     */
    private List<HotArticleVo> computeHotArticle(List<ApArticle> articleList) {
        List<HotArticleVo> hotArticleVos = new ArrayList<>();

        for (ApArticle apArticle : articleList) {
            HotArticleVo vo = new HotArticleVo();
            BeanUtils.copyProperties(apArticle, vo);

            vo.setScore(computeScore(apArticle));
            hotArticleVos.add(vo);
        }
        return hotArticleVos;
    }

    /**
     * 计算单个文章的分值
     * view->1分
     * like->3分
     * comment->5分
     * collection->8分
     * @param apArticle
     * @return
     */
    private Integer computeScore(ApArticle apArticle) {
        int score = 0;
        if (apArticle.getViews() != null) {
            score += apArticle.getViews() * ArticleConstants.HOT_ARTICLE_VIEW_WEIGHT;
        }
        if (apArticle.getLikes() != null) {
            score += apArticle.getLikes() * ArticleConstants.HOT_ARTICLE_LIKE_WEIGHT;
        }
        if (apArticle.getComment() != null) {
            score += apArticle.getComment() * ArticleConstants.HOT_ARTICLE_COMMENT_WEIGHT;
        }
        if (apArticle.getCollection() != null) {
            score += apArticle.getCollection() * ArticleConstants.HOT_ARTICLE_COLLECTION_WEIGHT;
        }
        return new Integer(score);
    }
}
