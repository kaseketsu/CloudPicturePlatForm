package com.itflower.cloudpicturebackend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用分页请求
 */
@Data
public class PageRequest implements Serializable {

    private static final long serialVersionUID = 2872966004506579653L;

    /**
     * 当前页面
     */
    private long current = 1L;

    /**
     * 页面大小
     */
    private long pageSize = 10L;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序(默认降序)
     */
    private String sortOrder = "descend";
}
