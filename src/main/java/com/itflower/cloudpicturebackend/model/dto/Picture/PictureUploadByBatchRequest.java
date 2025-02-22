package com.itflower.cloudpicturebackend.model.dto.Picture;

import lombok.Data;

import java.io.Serializable;

/**
 * 批量导入图片请求
 */
@Data
public class PictureUploadByBatchRequest implements Serializable {

    private static final long serialVersionUID = 3263180372982547170L;

    /**
     * 批量抓取数量(默认10条)
     */
    private Integer count = 10;

    /**
     * 搜索关键词
     */
    private String searchText;

    /**
     * 图片名称前缀
     */
    private String namePrefix;
}
