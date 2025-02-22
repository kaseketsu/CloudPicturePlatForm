package com.itflower.cloudpicturebackend.api.imagesearch.model;

import lombok.Data;

/**
 * 图片搜索结果
 */
@Data
public class ImageResult {

    /**
     * 缩略图url
     */
    private String thumbUrl;

    /**
     * 图片来源
     */
    private String fromUrl;
}
