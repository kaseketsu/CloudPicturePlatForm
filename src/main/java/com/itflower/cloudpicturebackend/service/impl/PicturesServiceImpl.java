package com.itflower.cloudpicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itflower.cloudpicturebackend.common.ResultUtils;
import com.itflower.cloudpicturebackend.exception.BusinessException;
import com.itflower.cloudpicturebackend.exception.ErrorCode;
import com.itflower.cloudpicturebackend.exception.ThrowUtil;
import com.itflower.cloudpicturebackend.manager.CosManager;
import com.itflower.cloudpicturebackend.manager.FileManager;
import com.itflower.cloudpicturebackend.manager.upload.FilePictureUpload;
import com.itflower.cloudpicturebackend.manager.upload.PictureUploadTemplate;
import com.itflower.cloudpicturebackend.manager.upload.UrlPictureUpload;
import com.itflower.cloudpicturebackend.model.dto.Picture.*;
import com.itflower.cloudpicturebackend.model.dto.file.PictureUploadResult;
import com.itflower.cloudpicturebackend.model.dto.space.SpaceAddRequest;
import com.itflower.cloudpicturebackend.model.entity.Pictures;
import com.itflower.cloudpicturebackend.model.entity.Space;
import com.itflower.cloudpicturebackend.model.entity.User;
import com.itflower.cloudpicturebackend.model.enums.PictureReviewStatusEnum;
import com.itflower.cloudpicturebackend.model.enums.SpaceLevelEnum;
import com.itflower.cloudpicturebackend.model.vo.PictureVO;
import com.itflower.cloudpicturebackend.model.vo.UserVO;
import com.itflower.cloudpicturebackend.service.PicturesService;
import com.itflower.cloudpicturebackend.mapper.PicturesMapper;
import com.itflower.cloudpicturebackend.service.SpaceService;
import com.itflower.cloudpicturebackend.service.UserService;
import com.itflower.cloudpicturebackend.utils.ColorSimilarUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 75574
 * @description 针对表【pictures(用户)】的数据库操作Service实现
 * @createDate 2024-12-31 21:52:14
 */
@Slf4j
@Service
public class PicturesServiceImpl extends ServiceImpl<PicturesMapper, Pictures>
        implements PicturesService {

    @Resource
    private UrlPictureUpload urlPictureUpload;

    @Resource
    private FilePictureUpload filePictureUpload;

    @Resource
    private SpaceService spaceService;

    @Resource
    private UserService userService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Autowired
    private CosManager cosManager;


    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest request, User loginUser) {
        //校验参数
        ThrowUtil.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR, "用户无权限");
        //校验空间是否存在
        Long spaceId = request.getSpaceId();
        if (ObjUtil.isNotNull(spaceId)) {
            Space space = spaceService.getById(spaceId);
            ThrowUtil.throwIf(ObjUtil.isNull(space), ErrorCode.NOT_FOUND_ERROR, "指定空间不存在");
            //校验是否有权限
            if (!loginUser.getId().equals(space.getUserId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间权限");
            }
            //校验空间额度
            ThrowUtil.throwIf(space.getTotalSize() >= space.getMaxSize(), ErrorCode.OPERATION_ERROR,
                    "剩余存储容量不足");
            ThrowUtil.throwIf(space.getTotalCount() >= space.getMaxCount(), ErrorCode.OPERATION_ERROR,
                    "剩余存储数量不足");
        }
        //判断是新增还是删除
        Long picId = null;
        if (request != null) {
            picId = request.getId();
        }

        //更新请求, 判断图片是否存在
        if (picId != null) {
            Pictures oldPicture = this.getById(picId);
            ThrowUtil.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片为空");
            //仅本人和管理员
            if (!oldPicture.getUserID().equals(loginUser.getId()) || !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
            //校验新旧图片spaceId是否一致
            if (ObjUtil.isNull(spaceId)) {
                if (oldPicture.getSpaceId() != null) {
                    spaceId = oldPicture.getSpaceId();
                }
            } else {
                ThrowUtil.throwIf(ObjUtil.notEqual(spaceId, oldPicture.getSpaceId()),
                        ErrorCode.PARAMS_ERROR, "空间id不一致");
            }
        }

        //上传图片
        //创建目录前缀
        String uploadPathPrefix;
        if (spaceId == null) {
            uploadPathPrefix = String.format("public/%s", loginUser.getId());
        } else {
            uploadPathPrefix = String.format("space/%s", spaceId);
        }
        //根据参数类型选择上传方法
        PictureUploadTemplate pictureUploadTemplate = inputSource instanceof String ? urlPictureUpload : filePictureUpload;
        PictureUploadResult pictureUploadResult = pictureUploadTemplate.uploadPicture(inputSource, uploadPathPrefix);
        //构造图片信息
        Pictures pictures = new Pictures();
        pictures.setSpaceId(spaceId);
        pictures.setUrl(pictureUploadResult.getUrl());
        pictures.setThumbnailUrl(pictureUploadResult.getThumbnailUrl());
        String picName = pictureUploadResult.getPicName();
        //支持外层传递图片名称
        if (ObjUtil.isNotNull(request) && StrUtil.isNotBlank(request.getPicName())) {
            picName = request.getPicName();
        }
        pictures.setName(picName);
        pictures.setPicSize(pictureUploadResult.getPicSize());
        pictures.setPicWidth(pictureUploadResult.getPicWidth());
        pictures.setPicHeight(pictureUploadResult.getPicHeight());
        pictures.setPicScale(pictureUploadResult.getPicScale());
        pictures.setPicFormat(pictureUploadResult.getPicFormat());
        pictures.setPicColor(pictureUploadResult.getPicColor());
        pictures.setUserID(loginUser.getId());

        //补充审核参数
        fillReviewParams(pictures, loginUser);

        //操作数据库
        //id不为空表示更新, 否则创建
        if (picId != null) {
            pictures.setId(picId);
            pictures.setEditTime(new Date());
        }

        //用事务更新额度, 防止状态不一致，只有私有空间需要更新
        Long finalSpaceId = spaceId;

        transactionTemplate.execute(status -> {
            boolean b = this.saveOrUpdate(pictures);
            ThrowUtil.throwIf(!b, ErrorCode.SYSTEM_ERROR, "图片上传失败, 数据库操作失败");
            if (finalSpaceId != null) {
                boolean updateResult = spaceService.lambdaUpdate()
                        .eq(Space::getId, finalSpaceId)
                        .setSql("totalSize = totalSize + " + pictures.getPicSize())
                        .setSql("totalCount = totalCount + 1")
                        .update();
                ThrowUtil.throwIf(!updateResult, ErrorCode.SYSTEM_ERROR, "额度更新失败");
            }
            return true;
        });


        return PictureVO.ObjToVO(pictures);
    }

    @Override
    public QueryWrapper<Pictures> getQueryWrapper(PictureQueryRequest request) {
        //判断传递参数是否为空
        ThrowUtil.throwIf(request == null, ErrorCode.PARAMS_ERROR, "请求参数为空");

        //获取所有参数
        Long id = request.getId();
        String name = request.getName();
        String introduction = request.getIntroduction();
        List<String> tags = request.getTags();
        String category = request.getCategory();
        Integer picWidth = request.getPicWidth();
        Integer picHeight = request.getPicHeight();
        Double picScale = request.getPicScale();
        String picFormat = request.getPicFormat();
        String searchText = request.getSearchText();
        Long userId = request.getUserId();
        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();
        Integer reviewStatus = request.getReviewStatus();
        Long spaceId = request.getSpaceId();
        boolean nullSpaceId = request.isNullSpaceId();
        Long reviewId = request.getReviewId();
        String reviewMessage = request.getReviewMessage();
        Date startEditTime = request.getStartEditTime();
        Date endEditTime = request.getEndEditTime();

        //封装query
        QueryWrapper<Pictures> pictureQuery = new QueryWrapper<>();
        if (StrUtil.isNotBlank(searchText)) {
            //拼接查询条件
            pictureQuery.and(qw -> qw.like("name", searchText)
                    .or()
                    .like("introduction", searchText)
            );
        }

        pictureQuery.eq(ObjUtil.isNotNull(id), "id", id);
        pictureQuery.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        pictureQuery.eq(ObjUtil.isNotEmpty(spaceId), "spaceId", spaceId);
        pictureQuery.isNull(nullSpaceId, "spaceId");
        pictureQuery.like(StrUtil.isNotBlank(name), "name", name);
        pictureQuery.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        pictureQuery.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        pictureQuery.like(ObjUtil.isNotEmpty(reviewMessage), "reviewMessage", reviewMessage);
        pictureQuery.eq(StrUtil.isNotBlank(category), "category", category);
        pictureQuery.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        pictureQuery.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        pictureQuery.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);
        pictureQuery.eq(ObjUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        pictureQuery.eq(ObjUtil.isNotEmpty(reviewId), "reviewId", reviewId);
        // >= 开始编辑时间
        pictureQuery.ge(ObjUtil.isNotEmpty(startEditTime), "editTime", startEditTime);
        // < 结束编辑时间
        pictureQuery.lt(ObjUtil.isNotEmpty(endEditTime), "editTime", endEditTime);

        //Json数组查询
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                pictureQuery.like("tags", "\"" + tag + "\"");
            }
        }
        pictureQuery.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return pictureQuery;
    }

    /**
     * 获取图片脱敏信息
     *
     * @param pictures 图片实体类
     * @param request  http请求
     * @return 脱敏视图
     */
    @Override
    public PictureVO getPictureVO(Pictures pictures, HttpServletRequest request) {
        //转换为VO类
        PictureVO pictureVO = PictureVO.ObjToVO(pictures);
        //关联查询用户信息
        Long userID = pictures.getUserID();
        if (userID != null && userID > 0) {
            User user = userService.getById(userID);
            UserVO userVO = userService.getUserVO(user);
            pictureVO.setUserVO(userVO);
        }
        return pictureVO;
    }

    @Override
    public Page<PictureVO> getPictureVOPage(Page<Pictures> picturesPage, HttpServletRequest request) {
        List<Pictures> picturesList = picturesPage.getRecords();
        //创新空的PageVO
        Page<PictureVO> pictureVOPage = new Page<>(picturesPage.getCurrent(), picturesPage.getSize(), picturesPage.getTotal());
        if (CollUtil.isEmpty(picturesList)) {
            return pictureVOPage;
        }
        //将picture列表转换为pictureVO列表
        List<PictureVO> pictureVOList = picturesList.stream().map(PictureVO::ObjToVO).collect(Collectors.toList());
        //对用户id进行去重
        Set<Long> userIdSet = picturesList.stream().map(Pictures::getUserID).collect(Collectors.toSet());
        Map<Long, List<User>> userIdAndListMap = userService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        //填充pictureVO的userVO
        pictureVOList.forEach(pictureVO -> {
            Long userID = pictureVO.getUserID();
            User user = null;
            if (userIdAndListMap.containsKey(userID)) {
                user = userIdAndListMap.get(userID).get(0);
            }
            pictureVO.setUserVO(userService.getUserVO(user));
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }

    /**
     * 校验图片
     *
     * @param pictures 图片信息封装类
     */
    @Override
    public void validPicture(Pictures pictures) {
        ThrowUtil.throwIf(pictures == null, ErrorCode.PARAMS_ERROR, "图片信息不能为空");
        //取值
        Long id = pictures.getId();
        String url = pictures.getUrl();
        String introduction = pictures.getIntroduction();
        //分别对取值判断合法性
        ThrowUtil.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "图片id不能为空");
        if (StrUtil.isNotBlank(url)) {
            ThrowUtil.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "图片url过长");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtil.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "图片介绍过长");
        }
    }

    @Override
    public void doPictureReview(PictureReviewRequest request, User loginUser) {
        //1.校验参数
        ThrowUtil.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        Long id = request.getId();
        Integer reviewStatus = request.getReviewStatus();
        String reviewMessage = request.getReviewMessage();
        PictureReviewStatusEnum statusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
        if (id == null || statusEnum == null || statusEnum.equals(PictureReviewStatusEnum.REVIEWING)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2.判断图片是否存在
        Pictures oldPictures = this.getById(id);
        ThrowUtil.throwIf(oldPictures == null, ErrorCode.PARAMS_ERROR);
        //3.避免重复审核
        if (oldPictures.getReviewStatus().equals(request.getReviewStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "请勿重复审核");
        }
        //4.数据库操作
        Pictures updatePicture = new Pictures();
        BeanUtil.copyProperties(request, updatePicture);
        updatePicture.setReviewId(loginUser.getId());
        updatePicture.setReviewTime(new Date());
        updatePicture.setReviewMessage(reviewMessage);
        boolean b = this.updateById(updatePicture);
        ThrowUtil.throwIf(!b, ErrorCode.OPERATION_ERROR, "审核失败");
    }

    /**
     * 填充审核参数
     *
     * @param pictures  传入图片
     * @param loginUser 用户信息
     */
    @Override
    public void fillReviewParams(Pictures pictures, User loginUser) {
        if (userService.isAdmin(loginUser)) {
            pictures.setReviewMessage("管理员自动过审");
            pictures.setReviewStatus(PictureReviewStatusEnum.ACCEPT.getValue());
            pictures.setReviewId(loginUser.getId());
            pictures.setReviewTime(new Date());
        } else {
            pictures.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }

    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
        //校验参数
        String searchText = pictureUploadByBatchRequest.getSearchText();
        Integer count = pictureUploadByBatchRequest.getCount();
        ThrowUtil.throwIf(count > 30 || count < 0, ErrorCode.PARAMS_ERROR, "图片抓取数量不合法");
        String namePrefix = pictureUploadByBatchRequest.getNamePrefix();
        if (StrUtil.isNotBlank(searchText)) {
            //名称前缀默认为searchText
            namePrefix = searchText;
        }
        //抓取内容
        String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1", searchText);
        Document document;
        try {
            document = Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            log.error("获取页面文档失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取页面文档失败");
        }
        //解析内容
        Elements div = document.getElementsByClass("dgControl");
        ThrowUtil.throwIf(ObjUtil.isNull(div), ErrorCode.OPERATION_ERROR, "获取元素失败");
        Elements imgElementList = div.select("img.mimg");
        //上传图片
        int uploadCount = 0;
        for (Element imgElement : imgElementList) {
            String imgUrl = imgElement.attr("src");
            if (StrUtil.isBlank(imgUrl)) {
                log.info("当前链接为空, 已跳过");
                continue;
            }
            //获取图片地址, 并进行处理
            int questionMarkIndex = imgUrl.indexOf("?");
            if (questionMarkIndex > -1) {
                imgUrl = imgUrl.substring(0, questionMarkIndex);
            }
            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
            pictureUploadRequest.setFileUrl(imgUrl);
            pictureUploadRequest.setPicName(namePrefix + (uploadCount + 1));

            try {
                PictureVO pictureVO = uploadPicture(imgUrl, pictureUploadRequest, loginUser);
                log.info("图片上传成功, id = {}", pictureVO.getId());
                uploadCount++;
            } catch (Exception e) {
                log.error("图片上传失败", e);
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "图片上传失败");
            }
            if (uploadCount >= count) {
                break;
            }
        }
        return uploadCount;
    }

    /**
     * 清理图片
     *
     * @param oldPicture 待清理图片
     */
    @Async
    @Override
    public void clearPictureFile(Pictures oldPicture) {
        //判断该图片是否被多条记录使用
        String url = oldPicture.getUrl();
        Long count = this.lambdaQuery()
                .eq(Pictures::getUrl, url)
                .count();
        if (count > 1) {
            return;
        }
        //删除webp文件和缩略图
        cosManager.deleteObject(url);
        String thumbnailUrl = oldPicture.getThumbnailUrl();
        if (StrUtil.isNotBlank(thumbnailUrl)) {
            cosManager.deleteObject(thumbnailUrl);
        }
    }

    @Override
    public void checkPictureAuth(User loginUser, Pictures pictures) {
        Long userId = loginUser.getId();

        if (!userId.equals(pictures.getUserID()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

    }

    @Override
    public void deletePicture(long pictureId, User loginUser) {
        //校验参数
        ThrowUtil.throwIf(pictureId == 0, ErrorCode.PARAMS_ERROR);
        ThrowUtil.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        //判断图片是否存在
        Pictures deletePicture = this.getById(pictureId);
        ThrowUtil.throwIf(deletePicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        //判断权限
        this.checkPictureAuth(loginUser, deletePicture);

        //用事务更新额度, 防止状态不一致
        Long finalSpaceId = deletePicture.getSpaceId();
        transactionTemplate.execute(status -> {
            //操作数据库
            boolean b = this.removeById(pictureId);
            ThrowUtil.throwIf(!b, ErrorCode.OPERATION_ERROR);
            if (finalSpaceId != null) {
                boolean updateResult = spaceService.lambdaUpdate()
                        .eq(Space::getId, finalSpaceId)
                        .setSql("totalSize = totalSize - " + deletePicture.getPicSize())
                        .setSql("totalCount = totalCount - 1")
                        .update();
                ThrowUtil.throwIf(!updateResult, ErrorCode.SYSTEM_ERROR, "额度更新失败");
            }
            return true;
        });

        //清理图片资源
        this.clearPictureFile(deletePicture);
    }

    @Override
    public void editPicture(PictureEditRequest pictureEditRequest, User loginUser) {
        //将dto类赋值给实体类
        Pictures pictures = new Pictures();
        BeanUtil.copyProperties(pictureEditRequest, pictures);
        pictures.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        pictures.setEditTime(new Date());
        //数据校验
        this.validPicture(pictures);
        Long id = pictures.getId();
        Pictures editPicture = this.getById(id);
        ThrowUtil.throwIf(editPicture == null, ErrorCode.NOT_FOUND_ERROR);
        //判断权限
        this.checkPictureAuth(loginUser, editPicture);
        //补充审核参数
        this.fillReviewParams(pictures, loginUser);
        //操作数据库
        boolean b = this.updateById(pictures);
        ThrowUtil.throwIf(!b, ErrorCode.OPERATION_ERROR);
    }

    @Override
    public List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser) {
        //校验参数
        ThrowUtil.throwIf(
                spaceId == null || StrUtil.isBlank(picColor),
                ErrorCode.PARAMS_ERROR
        );
        ThrowUtil.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        //校验权限
        Space space = spaceService.getById(spaceId);
        ThrowUtil.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        ThrowUtil.throwIf(!space.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR, "没有空间访问权限");
        //查询该空间下所有图片(有主色调)
        List<Pictures> picturesList = this.lambdaQuery()
                .eq(Pictures::getSpaceId, spaceId)
                .isNotNull(Pictures::getPicColor)
                .list();
        //没有图片则返回空, 将颜色字符串转换为主色调
        if (CollUtil.isEmpty(picturesList)) {
            return new ArrayList<>();
        }
        Color targetColor = Color.decode(picColor);
        //计算相似度并排序
        List<Pictures> sortedPicturesResultList = picturesList.stream()
                .sorted(Comparator.comparingDouble(picture -> {
                    String hexColor = picture.getPicColor();
                    if (StrUtil.isBlank(hexColor)) {
                        return Double.MAX_VALUE;
                    }
                    Color pictureColor = Color.decode(hexColor);
                    return -ColorSimilarUtils.calculateSimilarity(pictureColor, targetColor);
                }))
                .limit(12) //只取12条
                .collect(Collectors.toList());
        //返回查询到的图片列表
        return sortedPicturesResultList.stream()
                .map(PictureVO::ObjToVO)
                .collect(Collectors.toList()); 
    }

    @Override
    public void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser) {
        //校验参数
        Long spaceId = pictureEditByBatchRequest.getSpaceId();
        String category = pictureEditByBatchRequest.getCategory();
        List<String> tags = pictureEditByBatchRequest.getTags();
        List<Long> pictureIdList = pictureEditByBatchRequest.getPictureIdList();
        ThrowUtil.throwIf(spaceId == null, ErrorCode.PARAMS_ERROR);
        ThrowUtil.throwIf(CollUtil.isEmpty(pictureIdList), ErrorCode.PARAMS_ERROR);
        ThrowUtil.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        //校验权限
        Space space = spaceService.getById(spaceId);
        ThrowUtil.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        ThrowUtil.throwIf(!loginUser.getId().equals(space.getUserId()), ErrorCode.NO_AUTH_ERROR, "没有访问该空间的权限");
        //潮汛图片是否存在(id 和 spaceId字段)
        List<Pictures> picturesList = this.lambdaQuery()
                .select(Pictures::getId, Pictures::getSpaceId)
                .eq(Pictures::getSpaceId, spaceId)
                .in(Pictures::getId, pictureIdList)
                .list();
        if (CollUtil.isEmpty(picturesList)) {
            return;
        }
        //编辑标签和分类字段
        picturesList.forEach(pictures -> {
            if (StrUtil.isNotBlank(category)) {
                pictures.setCategory(category);
            }
            if (CollUtil.isNotEmpty(tags)) {
                pictures.setTags(JSONUtil.toJsonStr(tags));
            }
        });
        //批量重命名
        String nameRule = pictureEditByBatchRequest.getNameRule();
        fillPictureWithNameRule(picturesList, nameRule);
        //批量更新数据库
        boolean b = this.updateBatchById(picturesList);
        ThrowUtil.throwIf(!b, ErrorCode.OPERATION_ERROR, "批量编辑失败");
    }

    /**
     * nameRule: 格式：图片{序号}
     * @param picturesList
     * @param nameRule
     */
    private void fillPictureWithNameRule(List<Pictures> picturesList, String nameRule) {
        if (CollUtil.isEmpty(picturesList) || StrUtil.isBlank(nameRule)) {
            return;
        }
        long count = 1;
        try {
            for (var picture: picturesList) {
                String picName = nameRule.replaceAll("\\{序号}", String.valueOf(count++));
                picture.setName(picName);
            }
        } catch (Exception e) {
            log.error("名称解析失败, ", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "名称解析错误");
        }
    }
}




