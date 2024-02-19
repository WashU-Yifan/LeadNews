package com.heima.model.user.pojos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * APP用户粉丝表
 * </p>
 *
 * @author Ethan
 */
@Data
@TableName("ap_user_follow")
public class ApUserFollow implements Serializable {


    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;


    /**
     * 用户id
     */
    @TableField( "user_id")
    private Integer userId;

    /**
     * 粉丝Id
     */
    @TableField("follow_id")
    private Integer followId;

    /**
     * 粉丝名称
     */
    @TableField("follow_name")
    private String followName;

    /**
     * 关注度
     *  0 偶尔感兴趣
     *  1 一般
     *  2 经常
     *  3 高度
     */
    @TableField("level")
    private Short level;

    /**
     * 是否动态通知
     */
    @TableField("is_notice")
    private Short isNotice;

    /**
     * 注册时间
     */
    @TableField("created_time")
    private Date createdTime;
}
