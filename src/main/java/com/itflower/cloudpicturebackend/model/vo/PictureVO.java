package com.itflower.cloudpicturebackend.model.vo;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.itflower.cloudpicturebackend.model.entity.Pictures;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class PictureVO implements Serializable {

    private static final long serialVersionUID = 8881150182688348419L;

    /**
     * id
     */
    private Long id;

    /**
     * 图片url
     */
    private String url;

    /**
     * 缩略图url
     */
    private String thumbnailUrl;

    /**
     * 图片名
     */
    private String name;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 标签(json数组)
     */
    private List<String> tags;

    /**
     * 分类
     */
    private String category;

    /**
     * 图片体积
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private Integer picWidth;

    /**
     * 图片高度
     */
    private Integer picHeight;

    /**
     * 图片宽高比例
     */
    private Double picScale;

    /**
     * 图片格式
     */
    private String picFormat;

    /**
     * 图片主色调
     */
    private String picColor;

    /**
     * 创建用户id
     */
    private Long userID;

    /**
     * 图片对应的空间id
     */
    private Long spaceId;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 新建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建用户信息
     */
    private UserVO userVO;

    /**
     * 封装转实体类
     * @param pictureVO 用户封装类
     */
    public static Pictures VOToObj(PictureVO pictureVO) {
        if (pictureVO == null)
            return null;
        Pictures pictures = new Pictures();
        BeanUtil.copyProperties(pictureVO, pictures);
        pictures.setTags(JSONUtil.toJsonStr(pictureVO.getTags()));
        return pictures;
    }

    /**
     * 实体类转封装
     * @param pictures 实体类
     */
    public static PictureVO ObjToVO(Pictures pictures) {
        if (pictures == null)
            return null;
        PictureVO pictureVO = new PictureVO();
        BeanUtil.copyProperties(pictures, pictureVO);
        pictureVO.setTags(JSONUtil.toList(pictures.getTags(), String.class));
        return pictureVO;
    }
}
