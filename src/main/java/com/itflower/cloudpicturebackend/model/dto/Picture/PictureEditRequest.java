package com.itflower.cloudpicturebackend.model.dto.Picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 图片编辑请求
 */
@Data
public class PictureEditRequest implements Serializable {

    private static final long serialVersionUID = 797245349709477607L;

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
