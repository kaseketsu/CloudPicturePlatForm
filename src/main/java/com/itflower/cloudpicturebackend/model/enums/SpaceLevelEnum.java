package com.itflower.cloudpicturebackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum SpaceLevelEnum {

    COMMON("普通版", 0, 100, 100L * 1024 * 1024),
    PROFESSIONAL("专业版", 1, 1000, 1000L * 1024 * 1024),
    FLAGSHIP("旗舰版", 2, 10000, 10000L * 1024 * 1024);

    private final String comment;

    private final int value;

    private final long maxCount;

    private final long maxSize;

    /**
     *
     * @param comment 文本
     * @param value mark
     * @param maxCount 最大图片数目
     * @param maxSize 最大容量, 单位(kB)
     */
    SpaceLevelEnum(String comment, int value, long maxCount, long maxSize) {
        this.comment = comment;
        this.value = value;
        this.maxCount = maxCount;
        this.maxSize = maxSize;
    }

    /**
     * 根据value获取枚举对象
     * @param value
     * @return
     */
    public static SpaceLevelEnum getEnumByValue(int value) {
        for (SpaceLevelEnum spaceLevelEnum : SpaceLevelEnum.values()) {
            if (spaceLevelEnum.value == value) {
                return spaceLevelEnum;
            }
        }
        return null;
    }
}
