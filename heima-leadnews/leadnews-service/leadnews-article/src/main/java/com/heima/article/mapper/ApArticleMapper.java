package com.heima.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.pojos.ApArticle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface ApArticleMapper extends BaseMapper<ApArticle> {
    /**
     * 加载初始文章
     * @param type 1 加载更多 2 加载最新
     * @param articleHomeDto dto
     * @return
     */
    public List<ApArticle> loadArticleList(@Param("dto") ArticleHomeDto articleHomeDto, @Param("type") Short type);

    public List<ApArticle> findArticleListByLast5days(@Param("dayParam") Date date);
}
