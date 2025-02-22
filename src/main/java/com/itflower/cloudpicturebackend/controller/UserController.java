package com.itflower.cloudpicturebackend.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itflower.cloudpicturebackend.annotation.AuthCheck;
import com.itflower.cloudpicturebackend.common.BaseResponse;
import com.itflower.cloudpicturebackend.common.DeleteRequest;
import com.itflower.cloudpicturebackend.common.ResultUtils;
import com.itflower.cloudpicturebackend.constant.UserConstant;
import com.itflower.cloudpicturebackend.exception.ErrorCode;
import com.itflower.cloudpicturebackend.exception.ThrowUtil;
import com.itflower.cloudpicturebackend.model.dto.user.*;
import com.itflower.cloudpicturebackend.model.entity.User;
import com.itflower.cloudpicturebackend.model.vo.LoginUserVO;
import com.itflower.cloudpicturebackend.model.vo.UserVO;
import com.itflower.cloudpicturebackend.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户信息封装类
     * @return 报错或成功信息
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtil.throwIf(ObjUtil.isNull(userRegisterRequest), ErrorCode.PARAMS_ERROR);
        long result = userService.userRegister(userRegisterRequest);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest 用户信息封装类
     * @return 报错或成功信息
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest httpServletRequest) {
        ThrowUtil.throwIf(ObjUtil.isNull(userLoginRequest), ErrorCode.PARAMS_ERROR);
        LoginUserVO result = userService.userLogin(userLoginRequest, httpServletRequest);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前登录用户
     *
     * @param httpServletRequest http请求
     * @return 返回脱敏信息
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        return ResultUtils.success(userService.getLoginUserVO(loginUser));
    }

    /**
     * 用户注销
     *
     * @param httpServletRequest http请求
     * @return 布尔值
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest httpServletRequest) {
        ThrowUtil.throwIf(httpServletRequest == null, ErrorCode.PARAMS_ERROR);
        userService.userLogout(httpServletRequest);
        return ResultUtils.success(true);
    }

    /**
     * 用户添加
     *
     * @param request 用户信息封装类
     * @return 用户数据库id
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/add")
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest request) {
        ThrowUtil.throwIf(ObjUtil.isNull(request), ErrorCode.PARAMS_ERROR);
        //转换对象
        User user = new User();
        BeanUtil.copyProperties(request, user);
        //设置默认值
        final String password = "12345678";
        String encryptPassword = userService.getEncryptPassword(password);
        user.setUserPassword(encryptPassword);
        //插入数据库
        boolean res = userService.save(user);
        ThrowUtil.throwIf(!res, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());

    }

    /**
     * 用户获取
     *
     * @param id 用户id
     * @return 用户信息相应
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/get")
    public BaseResponse<User> getUserById(long id) {
        ThrowUtil.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtil.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 用户脱敏信息获取
     *
     * @param id 用户id
     * @return 用户脱敏信息相应
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id) {
        BaseResponse<User> userBaseResponse = getUserById(id);
        User user = userBaseResponse.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 用户删除
     *
     * @param deleteRequest 用户删除请求
     * @return 是否删除
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(DeleteRequest deleteRequest) {
        ThrowUtil.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        boolean res = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(res);
    }

    /**
     * 用户更新
     *
     * @param request 用户信息封装类
     * @return 是否更新成功
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/updata")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest request) {
        ThrowUtil.throwIf(ObjUtil.isNull(request) || request.getId() == null, ErrorCode.PARAMS_ERROR);
        //转换对象
        User user = new User();
        BeanUtil.copyProperties(request, user);
        //更新用户
        boolean res = userService.updateById(user);
        ThrowUtil.throwIf(!res, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(res);
    }

    /**
     * 分页获取用户脱敏信息
     * @param request 用户请求
     * @return 分页查询结果
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserVO>> listUserByPage(@RequestBody UserQueryRequest request) {
        ThrowUtil.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        long current = request.getCurrent();
        long pageSize = request.getPageSize();
        //获取userPage
        Page<User> userPage = userService.page(new Page<>(current, pageSize), userService.getQueryWrapper(request));
        //转换为userVOList和userVOPage
        Page<UserVO> userVOPage = new Page<>(current, pageSize, userPage.getTotal());
        List<User> userList = userPage.getRecords();
        List<UserVO> userVOList = userService.getUserVOList(userList);
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }
}
