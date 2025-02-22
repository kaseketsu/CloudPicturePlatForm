package com.itflower.cloudpicturebackend.model.dto.Picture;

import lombok.Data;

import java.io.Serializable;

/**
 * 以图搜图请求
 */
@Data
public class SearchPictureByPictureRequest implements Serializable {

    private static final long serialVersionUID = -4861812912829779977L;

    /**
     * 搜索图片id
     */
    private Long pictureId;
}
