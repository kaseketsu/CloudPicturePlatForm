package com.itflower.cloudpicturebackend.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itflower.cloudpicturebackend.annotation.AuthCheck;
import com.itflower.cloudpicturebackend.common.BaseResponse;
import com.itflower.cloudpicturebackend.common.DeleteRequest;
import com.itflower.cloudpicturebackend.common.ResultUtils;
import com.itflower.cloudpicturebackend.constant.UserConstant;
import com.itflower.cloudpicturebackend.exception.ErrorCode;
import com.itflower.cloudpicturebackend.exception.ThrowUtil;
import com.itflower.cloudpicturebackend.model.dto.space.*;
import com.itflower.cloudpicturebackend.model.entity.Space;
import com.itflower.cloudpicturebackend.model.entity.User;
import com.itflower.cloudpicturebackend.model.enums.SpaceLevelEnum;
import com.itflower.cloudpicturebackend.model.vo.SpaceVO;
import com.itflower.cloudpicturebackend.service.SpaceService;
import com.itflower.cloudpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/space")
public class SpaceController {

    @Resource
    private UserService userService;

    @Resource
    private SpaceService spacesService;

    @PostMapping("/add")
    public BaseResponse<Long> addSpace(
            @RequestBody SpaceAddRequest spaceAddRequest,
            HttpServletRequest httpServletRequest
    ) {
        ThrowUtil.throwIf(ObjUtil.isNull(spaceAddRequest), ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpServletRequest);
        long addId = spacesService.addSpace(spaceAddRequest, loginUser);
        return ResultUtils.success(addId);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteSpace(
            @RequestBody DeleteRequest deleteRequest,
            HttpServletRequest httpServletRequest
    ) {
        //对请求判空
        ThrowUtil.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        //判断空间是否存在
        Long id = deleteRequest.getId();
        Space deleteSpace = spacesService.getById(id);
        ThrowUtil.throwIf(deleteSpace == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        //判断权限
        User loginUser = userService.getLoginUser(httpServletRequest);
        ThrowUtil.throwIf(
                !loginUser.getId().equals(deleteSpace.getUserId()) &&
                        !userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR
        );
        //操作数据库
        boolean b = spacesService.removeById(id);
        ThrowUtil.throwIf(!b, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * (仅管理员)更新空间
     *
     * @param updateRequest      更新信息封装类
     * @param httpServletRequest http请求
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateSpace(
            @RequestBody SpaceUpdateRequest updateRequest,
            HttpServletRequest httpServletRequest
    ) {
        ThrowUtil.throwIf(updateRequest == null || updateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        //将dto类转换为实体类
        Space spaces = new Space();
        BeanUtil.copyProperties(updateRequest, spaces);
        //自动填充数据
        spacesService.fillSpaceByLevel(spaces);
        //数据检验
        spacesService.validSpace(spaces, false);
        Long id = spaces.getId();
        //检查待更新空间是否存在
        Space oldSpace = spacesService.getById(id);
        ThrowUtil.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        //惭怍数据库
        boolean b = spacesService.updateById(spaces);
        ThrowUtil.throwIf(!b, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }


    @PostMapping("/edit")
    public BaseResponse<Boolean> editSpace(
            @RequestBody SpaceEditRequest spaceEditRequest,
            HttpServletRequest httpServletRequest
    ) {
        //对请求判空
        ThrowUtil.throwIf(spaceEditRequest == null || spaceEditRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        //将dto类赋值给实体类
        Space spaces = new Space();
        BeanUtil.copyProperties(spaceEditRequest, spaces);
        //数据填充
        spacesService.fillSpaceByLevel(spaces);
        spaces.setEditTime(new Date());
        //数据校验
        spacesService.validSpace(spaces, false);
        Long id = spaces.getId();
        Space editSpace = spacesService.getById(id);
        ThrowUtil.throwIf(editSpace == null, ErrorCode.NOT_FOUND_ERROR);
        //判断权限
        User loginUser = userService.getLoginUser(httpServletRequest);
        ThrowUtil.throwIf(
                !loginUser.getId().equals(editSpace.getUserId()) &&
                        !userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR
        );
        //操作数据库
        boolean b = spacesService.updateById(spaces);
        ThrowUtil.throwIf(!b, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 获取未脱敏空间(仅管理员)
     *
     * @param id 空间id
     * @return 空间信息
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Space> getSpaceById(long id) {
        ThrowUtil.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        //获取空间
        Space getSpace = spacesService.getById(id);
        ThrowUtil.throwIf(getSpace == null, ErrorCode.OPERATION_ERROR);
        //返回空间
        return ResultUtils.success(getSpace);
    }

    /**
     * 获取脱敏空间
     *
     * @param id      空间id
     * @param request http 请求
     * @return 脱敏空间信息
     */
    @GetMapping("/get/vo")
    public BaseResponse<SpaceVO> getSpaceVOById(long id, HttpServletRequest request) {
        ThrowUtil.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        //获取空间
        Space getSpace = spacesService.getById(id);
        ThrowUtil.throwIf(getSpace == null, ErrorCode.OPERATION_ERROR);
        //返回空间
        return ResultUtils.success(spacesService.getspaceVO(getSpace, request));
    }

    /**
     * 分页获取空间列表(仅管理员可用)
     *
     * @param request 空间查询请求
     * @return 分页空间封装
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Space>> listSpaceByPage(@RequestBody SpaceQueryRequest request) {
        ThrowUtil.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        //构造Page
        long current = request.getCurrent();
        long pageSize = request.getPageSize();
        Page<Space> spacesPage = spacesService.page(new Page<>(current, pageSize),
                spacesService.getQueryWrapper(request));
        return ResultUtils.success(spacesPage);
    }

    /**
     * 分页获取空间脱敏视图
     *
     * @param request            空间查询请求
     * @param httpServletRequest http请求
     * @return 脱敏列表
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<SpaceVO>> listSpaceVOByPage(
            @RequestBody SpaceQueryRequest request,
            HttpServletRequest httpServletRequest
    ) {
        ThrowUtil.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        //构造Page
        long current = request.getCurrent();
        long pageSize = request.getPageSize();
        //防止爬虫
        ThrowUtil.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR);
        Page<Space> spacesPage = spacesService.page(new Page<>(current, pageSize),
                spacesService.getQueryWrapper(request));
        return ResultUtils.success(spacesService.getspaceVOPage(spacesPage, httpServletRequest));
    }

    /**
     * 获取空间级别对象
     * @return 空间级别对象
     */
    @GetMapping("/list/level")
    public BaseResponse<List<SpaceLevel>> listSpaceLevel() {
        List<SpaceLevel> spaceLevelList = Arrays.stream(SpaceLevelEnum.values())
                .map(spaceLevelEnum -> new SpaceLevel(
                        spaceLevelEnum.getValue(),
                        spaceLevelEnum.getComment(),
                        spaceLevelEnum.getMaxCount(),
                        spaceLevelEnum.getMaxSize()
                ))
                .collect(Collectors.toList());
        return ResultUtils.success(spaceLevelList);
    }

}
