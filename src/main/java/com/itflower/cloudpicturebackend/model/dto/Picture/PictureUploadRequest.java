package com.itflower.cloudpicturebackend.model.dto.Picture;

import com.itflower.cloudpicturebackend.model.vo.UserVO;
import lombok.Data;

import java.io.Serializable;

@Data
public class PictureUploadRequest implements Serializable {

    private static final long serialVersionUID = -2249694003045562800L;

    /**
     * id
     */
    private Long id;

    /**
     * 空间id
     */
    private Long spaceId;

    /**
     * 图片名称
     */
    private String picName;

    /**
     * 图片url
     */
    private String fileUrl;
}
