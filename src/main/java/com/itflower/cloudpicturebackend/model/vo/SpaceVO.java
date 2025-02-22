package com.itflower.cloudpicturebackend.model.vo;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.itflower.cloudpicturebackend.model.entity.Pictures;
import com.itflower.cloudpicturebackend.model.entity.Space;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 空间视图
 */
@Data
public class SpaceVO implements Serializable {

    private static final long serialVersionUID = 999413419086000873L;

    /**
     * id
     */
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;

    /**
     * 空间图片的最大总大小
     */
    private Long maxSize;

    /**
     * 空间图片的最大数量
     */
    private Long maxCount;

    /**
     * 当前空间下图片的总大小
     */
    private Long totalSize;

    /**
     * 当前空间下的图片数量
     */
    private Long totalCount;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 用户信息
     */
    private UserVO userVO;



    /**
     * 封装转实体类
     * @param spaceVO 空间封装类
     */
    public static Space VOToObj(SpaceVO spaceVO) {
        if (spaceVO == null)
            return null;
        Space space = new Space();
        BeanUtil.copyProperties(spaceVO, space);;
        return space;
    }

    /**
     * 实体类转封装
     * @param space 实体类
     */
    public static SpaceVO ObjToVO(Space space) {
        if (space == null)
            return null;
        SpaceVO spaceVO = new SpaceVO();
        BeanUtil.copyProperties(space, spaceVO);
        return spaceVO;
    }
}
