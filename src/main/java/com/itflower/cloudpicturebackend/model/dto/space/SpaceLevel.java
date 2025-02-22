package com.itflower.cloudpicturebackend.model.dto.space;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 空间级别
 */
@Data
@AllArgsConstructor
public class SpaceLevel{

    /**
     * 值
     */
    private int value;

    /**
     * 中文描述
     */
    private String text;

    /**
     * 最大数量(图片)
     */
    private long maxCount;

    /**
     * 最大容量(图片)
     */
    private long maxSize;
}
