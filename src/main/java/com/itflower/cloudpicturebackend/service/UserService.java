package com.itflower.cloudpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.itflower.cloudpicturebackend.model.dto.user.UserLoginRequest;
import com.itflower.cloudpicturebackend.model.dto.user.UserQueryRequest;
import com.itflower.cloudpicturebackend.model.dto.user.UserRegisterRequest;
import com.itflower.cloudpicturebackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itflower.cloudpicturebackend.model.vo.LoginUserVO;
import com.itflower.cloudpicturebackend.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

/**
* @author 75574
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2024-12-20 14:39:00
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param userRegisterRequest 用户注册信息封装
     * @return 数据库id
     */
    long userRegister(UserRegisterRequest userRegisterRequest);

    /**
     * 用户登录
     * @param userLoginRequest 用户登录封装
     * @param request 请求
     * @return 脱敏用户信息
     */
    LoginUserVO userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request);

    /**
     * 获得脱敏后用户信息
     * @param user 用户封装信息
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取脱敏后用户信息
     * @param user 用户信息
     * @return 脱敏
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏后用户信息列表
     * @param userList 用户信息列表
     * @return 脱敏后用户信息列表
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 密码加密
     * @param password 密码
     * @return 返回加密密码
     */
    String getEncryptPassword(String password);

    /**
     * 获取当前登录用户, 后端用
     * @param httpServletRequest servlet
     * @return 返回当前登录用户信息
     */
    User getLoginUser(HttpServletRequest httpServletRequest);

    /**
     * 用户注销
     * @param request 请求数据
     * @return 用户信息封装
     */
    void userLogout(HttpServletRequest request);

    /**
     * 用户查询操作
     * @param userQueryRequest 用户查询
     * @return 用户查询SQL语句
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 判断用户是否是管理员
     * @param user 用户封装信息
     * @return 布尔值
     */
    boolean isAdmin(User user);

}
