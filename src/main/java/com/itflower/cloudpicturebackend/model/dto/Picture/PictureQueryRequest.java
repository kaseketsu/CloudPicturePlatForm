package com.itflower.cloudpicturebackend.model.dto.Picture;

import com.itflower.cloudpicturebackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 图片查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PictureQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = -728659525382054553L;

    /**
     * id
     */
    private Long id;

    /**
     * 图片名
     */
    private String name;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 标签(json数组)
     */
    private List<String> tags;

    /**
     * 分类
     */
    private String category;

    /**
     * 文件大小
     */
    private Long PicSize;

    /**
     * 图片宽度
     */
    private Integer picWidth;

    /**
     * 图片高度
     */
    private Integer picHeight;

    /**
     * 图片宽高比例
     */
    private Double picScale;

    /**
     * 图片格式
     */
    private String picFormat;

    /**
     * 关键词 简介等
     */
    private String searchText;

    /**
     * 审核状态
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;

    /**
     * 审核员id
     */
    private Long reviewId;

    /**
     * 审核时间
     */
    private Date reviewTime;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 空间id
     */
    private Long spaceId;

    /**
     * 开始时间
     */
    private Date startEditTime;

    /**
     * 结束时间
     */
    private Date endEditTime;

    /**
     * 是否设置spaceId为null(公共空间)
     */
    private boolean nullSpaceId;
}
