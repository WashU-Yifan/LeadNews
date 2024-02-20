package com.heima.model.article.pojos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("ap_collection")
public class ApArticleCollection {

    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("entry_id")
    private Integer entryId;
    /**
     * 文章id
     */
    @TableField("article_id")
    private Long articleId;

    /**
     *  0文章
     *  1动态
     */
    @TableField("type")
    private Short type;

    /**
     * 收藏时间
     */
    @TableField("collection_time")
    private Date collectionTime;


    /**
     * 发布时间
     */
    @TableField("published_time")
    private Date publishedTime;



}
