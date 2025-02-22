package com.itflower.cloudpicturebackend.model.dto.space;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建空间请求
 */
@Data
public class SpaceAddRequest implements Serializable {

    private static final long serialVersionUID = -6332273619459616144L;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间级别
     */
    private String spaceLevel;
}
