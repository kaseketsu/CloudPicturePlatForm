package com.itflower.cloudpicturebackend.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.itflower.cloudpicturebackend.annotation.AuthCheck;
import com.itflower.cloudpicturebackend.api.imagesearch.ImageSearchApiFacade;
import com.itflower.cloudpicturebackend.api.imagesearch.model.ImageResult;
import com.itflower.cloudpicturebackend.common.BaseResponse;
import com.itflower.cloudpicturebackend.common.DeleteRequest;
import com.itflower.cloudpicturebackend.common.ResultUtils;
import com.itflower.cloudpicturebackend.constant.UserConstant;
import com.itflower.cloudpicturebackend.exception.ErrorCode;
import com.itflower.cloudpicturebackend.exception.ThrowUtil;
import com.itflower.cloudpicturebackend.model.dto.Picture.*;
import com.itflower.cloudpicturebackend.model.entity.Pictures;
import com.itflower.cloudpicturebackend.model.entity.Space;
import com.itflower.cloudpicturebackend.model.entity.User;
import com.itflower.cloudpicturebackend.model.enums.PictureReviewStatusEnum;
import com.itflower.cloudpicturebackend.model.vo.PictureTagCategory;
import com.itflower.cloudpicturebackend.model.vo.PictureVO;
import com.itflower.cloudpicturebackend.service.PicturesService;
import com.itflower.cloudpicturebackend.service.SpaceService;
import com.itflower.cloudpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Slf4j
@RestController
@RequestMapping("/picture")
public class PictureController {

    @Resource
    private UserService userService;

    @Resource
    private PicturesService picturesService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private SpaceService spaceService;

    private final Cache<String, String> LOCAL_CACHE = Caffeine.newBuilder()
            .initialCapacity(1024)
            .maximumSize(10_000L) //最大10000条
            .expireAfterWrite(Duration.ofMinutes(5)) //缓存五分钟后移除
            .build();

    /**
     * 普通用户上传图片
     * @param multipartFile
     * @param pictureUploadRequest
     * @param request
     * @return
     */
    @PostMapping("/upload/file")
    public BaseResponse<PictureVO> uploadPicture(
            @RequestPart("file") MultipartFile multipartFile,
            PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request
    ) {
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = picturesService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 普通用户上传图片(url)
     * @param pictureUploadRequest
     * @param request
     * @return
     */
    @PostMapping("/upload/url")
    public BaseResponse<PictureVO> uploadPictureByUrl(
            @RequestBody PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request
    ) {
        User loginUser = userService.getLoginUser(request);
        String fileUrl = pictureUploadRequest.getFileUrl();
        PictureVO pictureVO = picturesService.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(
            @RequestBody DeleteRequest deleteRequest,
            HttpServletRequest httpServletRequest
    ) {
        //对请求判空
        ThrowUtil.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        //获取图片id和用户
        Long id = deleteRequest.getId();
        User loginUser = userService.getLoginUser(httpServletRequest);
        //调佣方法
        picturesService.deletePicture(id, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * (仅管理员)更新图片
     *
     * @param updateRequest      更新信息封装类
     * @param httpServletRequest http请求
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(
            @RequestBody PictureUpdateRequest updateRequest,
            HttpServletRequest httpServletRequest
    ) {
        ThrowUtil.throwIf(updateRequest == null || updateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        //将dto类转换为实体类
        Pictures pictures = new Pictures();
        BeanUtil.copyProperties(updateRequest, pictures);
        pictures.setTags(JSONUtil.toJsonStr(updateRequest.getTags()));
        //数据检验
        picturesService.validPicture(pictures);
        Long id = pictures.getId();
        //检查待更新图片是否存在
        Pictures oldPicture = picturesService.getById(id);
        ThrowUtil.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        //补充审核参数
        User loginUser = userService.getLoginUser(httpServletRequest);
        picturesService.fillReviewParams(pictures, loginUser);
        //惭怍数据库
        boolean b = picturesService.updateById(pictures);
        ThrowUtil.throwIf(!b, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }


    @PostMapping("/edit")
    public BaseResponse<Boolean> editPicture(
            @RequestBody PictureEditRequest pictureEditRequest,
            HttpServletRequest httpServletRequest
    ) {
        //对请求判空
        ThrowUtil.throwIf(pictureEditRequest == null || pictureEditRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpServletRequest);
        picturesService.editPicture(pictureEditRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 获取未脱敏图片(仅管理员)
     *
     * @param id 图片id
     * @return 图片信息
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Pictures> getPictureById(long id) {
        ThrowUtil.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        //获取图片
        Pictures getPicture = picturesService.getById(id);
        ThrowUtil.throwIf(getPicture == null, ErrorCode.OPERATION_ERROR);
        //返回图片
        return ResultUtils.success(getPicture);
    }

    /**
     * 获取脱敏图片
     *
     * @param id      图片id
     * @param request http 请求
     * @return 脱敏图片信息
     */
    @GetMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVOById(long id, HttpServletRequest request) {
        ThrowUtil.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        //获取图片
        Pictures getPicture = picturesService.getById(id);
        ThrowUtil.throwIf(getPicture == null, ErrorCode.OPERATION_ERROR);
        //校验权限
        Long spaceId = getPicture.getSpaceId();
        if (spaceId != null) {
            User loginUser = userService.getLoginUser(request);
            picturesService.checkPictureAuth(loginUser, getPicture);
        }
        //返回图片
        return ResultUtils.success(picturesService.getPictureVO(getPicture, request));
    }

    /**
     * 分页获取图片列表(仅管理员可用)
     *
     * @param request 图片查询请求
     * @return 分页图片封装
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Pictures>> listPictureByPage(@RequestBody PictureQueryRequest request) {
        ThrowUtil.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        //构造Page
        long current = request.getCurrent();
        long pageSize = request.getPageSize();
        Page<Pictures> picturesPage = picturesService.page(new Page<>(current, pageSize),
                picturesService.getQueryWrapper(request));
        return ResultUtils.success(picturesPage);
    }

    /**
     * 分页获取图片脱敏视图
     *
     * @param request            图片查询请求
     * @param httpServletRequest http请求
     * @return 脱敏列表
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(
            @RequestBody PictureQueryRequest request,
            HttpServletRequest httpServletRequest
    ) {
        ThrowUtil.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        //构造Page
        long current = request.getCurrent();
        long pageSize = request.getPageSize();
        //防止爬虫
        ThrowUtil.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR);
        Long spaceId = request.getSpaceId();
        if (spaceId == null) {
            //普通用户默认只能看到审核通过的图片
            request.setReviewStatus(PictureReviewStatusEnum.ACCEPT.getValue());
            request.setNullSpaceId(true);
        } else {
            //校验权限
            User loginUser = userService.getLoginUser(httpServletRequest);
            Space space = spaceService.getById(spaceId);
            ThrowUtil.throwIf(space == null, ErrorCode.PARAMS_ERROR, "空间不存在");
            ThrowUtil.throwIf(
                    !loginUser.getId().equals(space.getUserId()) &&
                            !userService.isAdmin(loginUser),
                    ErrorCode.NO_AUTH_ERROR, "没有当前空间权限"
            );
            request.setNullSpaceId(false);
            request.setSpaceId(spaceId);
        }
        Page<Pictures> picturesPage = picturesService.page(new Page<>(current, pageSize),
                picturesService.getQueryWrapper(request));
        return ResultUtils.success(picturesService.getPictureVOPage(picturesPage, httpServletRequest));
    }

    /**
     * 分页获取图片脱敏视图(多级缓存)
     *
     * @param request            图片查询请求
     * @param httpServletRequest http请求
     * @return 脱敏列表
     */
    @Deprecated
    @PostMapping("/list/page/vo/cache")
    public BaseResponse<Page<PictureVO>> listPictureVOByPageWithCache(
            @RequestBody PictureQueryRequest request,
            HttpServletRequest httpServletRequest
    ) {
        ThrowUtil.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        //构造Page
        long current = request.getCurrent();
        long pageSize = request.getPageSize();
        //防止爬虫
        ThrowUtil.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR);
        //普通用户默认只能看到审核通过的图片
        request.setReviewStatus(PictureReviewStatusEnum.ACCEPT.getValue());

        //先查询缓存, 没有再查数据库
        //构造缓存的key
        String queryCondition = JSONUtil.toJsonStr(request);
        String hashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
        String cacheKey = String.format("flowerPicture:listPictureVOByPageWithCache:%s", hashKey);
        //先查本地缓存
        String cachedPage = LOCAL_CACHE.getIfPresent(cacheKey);
        if (ObjUtil.isNotNull(cachedPage)) {
            //反序列化为对象
            Page<PictureVO> picturesPage = JSONUtil.toBean(cachedPage, Page.class);
            return ResultUtils.success(picturesPage);
        }
        //再查redis缓存
        ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
        cachedPage = opsForValue.get(cacheKey);
        if (ObjUtil.isNotNull(cachedPage)) {
            //反序列化为对象
            Page<PictureVO> picturesPage = JSONUtil.toBean(cachedPage, Page.class);
            //更新本地缓存
            String cacheValue = JSONUtil.toJsonStr(picturesPage);
            LOCAL_CACHE.put(cacheKey, cacheValue);
            return ResultUtils.success(picturesPage);
        }

        //两个缓存都未命中, 查询数据库数据
        Page<Pictures> picturesPage = picturesService.page(new Page<>(current, pageSize),
                picturesService.getQueryWrapper(request));
        //查询数据写入redis和本地缓存
        Page<PictureVO> pictureVOPage = picturesService.getPictureVOPage(picturesPage, httpServletRequest);
        String cacheValue = JSONUtil.toJsonStr(picturesPage);
        LOCAL_CACHE.put(cacheKey, cacheValue);
        //设置redis缓存时间, 5 - 10min, 防止缓存雪崩
        int cacheExpireTime = 300 + RandomUtil.randomInt(0, 300);
        opsForValue.set(cacheKey, cacheValue, cacheExpireTime, TimeUnit.SECONDS);

        return ResultUtils.success(pictureVOPage);
    }

    /**
     * 页面标签和分类
     *
     * @return 标签分类
     */
    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
        List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
        pictureTagCategory.setCategoryList(categoryList);
        pictureTagCategory.setTagList(tagList);
        return ResultUtils.success(pictureTagCategory);
    }

    /**
     * 审核图片
     * @param request 图片审核信息
     * @param httpServletRequest http请求
     * @return true
     */
    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest request,
                                                       HttpServletRequest httpServletRequest) {
        ThrowUtil.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpServletRequest);
        picturesService.doPictureReview(request, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 图片批量抓取
     * @param request 图片信息 
     * @param httpServletRequest http请求
     * @return 抓取数量
     */
    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> uploadPictureByBatch(@RequestBody PictureUploadByBatchRequest request,
                                                 HttpServletRequest httpServletRequest) {
        ThrowUtil.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpServletRequest);
        Integer uploadCount = picturesService.uploadPictureByBatch(request, loginUser);
        return ResultUtils.success(uploadCount);
    }


    /**
     * 以图搜图接口
     * @param searchPictureByPictureRequest
     * @return
     */
    @PostMapping("/search/picture")
    public BaseResponse<List<ImageResult>> searchPictureByPicture(@RequestBody SearchPictureByPictureRequest searchPictureByPictureRequest) {
        ThrowUtil.throwIf(searchPictureByPictureRequest == null, ErrorCode.PARAMS_ERROR);
        Long pictureId = searchPictureByPictureRequest.getPictureId();
        ThrowUtil.throwIf(pictureId == null || pictureId <= 0, ErrorCode.PARAMS_ERROR);
        Pictures pictures = picturesService.getById(pictureId);
        ThrowUtil.throwIf(pictures == null, ErrorCode.PARAMS_ERROR);
        List<ImageResult> imageResults = ImageSearchApiFacade.searchImage(pictures.getThumbnailUrl());
        return ResultUtils.success(imageResults);
    }

    /**
     * 以色搜图接口
     * @param searchPictureByColorRequest
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/search/color")
    public BaseResponse<List<PictureVO>> searchPictureByColor(@RequestBody SearchPictureByColorRequest searchPictureByColorRequest,
                                                              HttpServletRequest httpServletRequest) {
        ThrowUtil.throwIf(searchPictureByColorRequest == null, ErrorCode.PARAMS_ERROR);
        String picColor = searchPictureByColorRequest.getPicColor();
        Long spaceId = searchPictureByColorRequest.getSpaceId();
        User loginUser = userService.getLoginUser(httpServletRequest);
        List<PictureVO> pictureVOList = picturesService.searchPictureByColor(spaceId, picColor, loginUser);
        return ResultUtils.success(pictureVOList);
    }

    /**
     * 批量编辑图片
     * @param pictureEditByBatchRequest
     * @param httpServletRequest
     * @return
     */
    @PostMapping("edit/batch")
    public BaseResponse<Boolean> editPictureByBatch(
            @RequestBody PictureEditByBatchRequest pictureEditByBatchRequest,
            HttpServletRequest httpServletRequest
    ) {
        ThrowUtil.throwIf(pictureEditByBatchRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpServletRequest);
        picturesService.editPictureByBatch(pictureEditByBatchRequest, loginUser);
        return ResultUtils.success(true);
    }
}
