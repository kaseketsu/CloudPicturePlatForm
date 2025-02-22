package com.itflower.cloudpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itflower.cloudpicturebackend.model.dto.Picture.*;
import com.itflower.cloudpicturebackend.model.dto.space.SpaceAddRequest;
import com.itflower.cloudpicturebackend.model.dto.user.UserQueryRequest;
import com.itflower.cloudpicturebackend.model.entity.Pictures;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itflower.cloudpicturebackend.model.entity.User;
import com.itflower.cloudpicturebackend.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 75574
 * @description 针对表【pictures(用户)】的数据库操作Service
 * @createDate 2024-12-31 21:52:14
 */
public interface PicturesService extends IService<Pictures> {

    /**
     * 上传图片
     *
     * @param inputSource 传输文件
     * @param request     图片上传请求
     * @param loginUser   登录用户
     * @return 图片脱敏视图
     */
    PictureVO uploadPicture(Object inputSource, PictureUploadRequest request, User loginUser);

    /**
     * 图片查询操作
     *
     * @param request 图片查询
     * @return 图片查询SQL语句
     */
    QueryWrapper<Pictures> getQueryWrapper(PictureQueryRequest request);

    /**
     * 获取图片脱敏视图
     *
     * @param pictures 图片实体类
     * @param request  http请求
     * @return 图片脱敏视图
     */
    PictureVO getPictureVO(Pictures pictures, HttpServletRequest request);

    /**
     * 分页获取图片脱敏封装
     *
     * @param picturesPage 图片分页封装
     * @param request      http请求
     * @return 分页图片脱敏封装
     */
    Page<PictureVO> getPictureVOPage(Page<Pictures> picturesPage, HttpServletRequest request);

    /**
     * 判断图片信息是否合法
     *
     * @param pictures 图片信息封装类
     */
    void validPicture(Pictures pictures);

    /**
     * 图片审核
     */
    void doPictureReview(PictureReviewRequest request, User loginUser);

    /**
     * 填充图片审核参数
     *
     * @param pictures
     * @param loginUser
     */
    void fillReviewParams(Pictures pictures, User loginUser);

    /**
     * 图片批量爬取上传
     *
     * @param pictureUploadByBatchRequest
     * @param loginUser
     * @return 上传成功的图片数
     */
    Integer uploadPictureByBatch(
            PictureUploadByBatchRequest pictureUploadByBatchRequest,
            User loginUser
    );

    /**
     * 清理数据库中的图片
     *
     * @param oldPicture 待清理图片
     */
    void clearPictureFile(Pictures oldPicture);

    /**
     * 校验能否操作当前图片
     *
     * @param loginUser
     * @param pictures
     */
    void checkPictureAuth(User loginUser, Pictures pictures);

    /**
     * 删除图片业务逻辑
     *
     * @param pictureId
     * @param loginUser
     */
    void deletePicture(long pictureId, User loginUser);

    /**
     * 编辑图片业务逻辑
     *
     * @param pictureEditRequest
     * @param loginUser
     */
    void editPicture(PictureEditRequest pictureEditRequest, User loginUser);

    /**
     * 根据颜色查询图片（私有空间内部）
     *
     * @param spaceId
     * @param picColor
     * @param loginUser
     * @return
     */
    List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser);

    /**
     * 批量编辑图片
     *
     * @param pictureEditByBatchRequest
     * @param loginUser
     */
    void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser);
}
