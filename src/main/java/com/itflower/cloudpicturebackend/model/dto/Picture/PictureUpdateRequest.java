package com.itflower.cloudpicturebackend.model.dto.Picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 图片更新请求
 */
@Data
public class PictureUpdateRequest implements Serializable {

    private static final long serialVersionUID = 4475323996156581471L;

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

}
