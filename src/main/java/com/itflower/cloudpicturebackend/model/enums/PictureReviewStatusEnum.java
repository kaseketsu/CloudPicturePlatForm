package com.itflower.cloudpicturebackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import com.itflower.cloudpicturebackend.exception.ErrorCode;
import com.itflower.cloudpicturebackend.exception.ThrowUtil;
import lombok.Getter;

/**
 * 图片审核状态枚举类
 */
@Getter
public enum PictureReviewStatusEnum {

    REVIEWING("审核中", 0),
    ACCEPT("同意", 1),
    REJECT("拒绝", 2);

    private final String comment;

    private final int value;

    PictureReviewStatusEnum(String comment, int value) {
        this.comment = comment;
        this.value = value;
    }

    /**
     * 根据value获取枚举对象
     * @param value
     * @return
     */
    public static PictureReviewStatusEnum getEnumByValue(Integer value) {

        if (value == null) {
            return null;
        }

        for (PictureReviewStatusEnum pictureReviewStatusEnum : PictureReviewStatusEnum.values()) {
            if (pictureReviewStatusEnum.value == value) {
                return pictureReviewStatusEnum;
            }
        }

        return null;
    }
}
