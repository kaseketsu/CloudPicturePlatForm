package com.itflower.cloudpicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itflower.cloudpicturebackend.constant.UserConstant;
import com.itflower.cloudpicturebackend.exception.BusinessException;
import com.itflower.cloudpicturebackend.exception.ErrorCode;
import com.itflower.cloudpicturebackend.exception.ThrowUtil;
import com.itflower.cloudpicturebackend.model.dto.user.UserLoginRequest;
import com.itflower.cloudpicturebackend.model.dto.user.UserQueryRequest;
import com.itflower.cloudpicturebackend.model.dto.user.UserRegisterRequest;
import com.itflower.cloudpicturebackend.model.entity.User;
import com.itflower.cloudpicturebackend.model.enums.UserRoleEnum;
import com.itflower.cloudpicturebackend.model.vo.LoginUserVO;
import com.itflower.cloudpicturebackend.model.vo.UserVO;
import com.itflower.cloudpicturebackend.service.UserService;
import com.itflower.cloudpicturebackend.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 75574
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2024-12-20 14:39:00
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户信息封装类
     * @return 数据库Id
     */
    @Override
    public long userRegister(UserRegisterRequest userRegisterRequest) {
        //1. 校验参数
        ThrowUtil.throwIf(StrUtil.hasBlank(userRegisterRequest.getUserAccount(),
                        userRegisterRequest.getUserPassword(), userRegisterRequest.getCheckPassword()),
                ErrorCode.PARAMS_ERROR,
                "参数为空");
        ThrowUtil.throwIf(userRegisterRequest.getUserAccount().length() < 4, ErrorCode.PARAMS_ERROR, "用户账户过短");
        ThrowUtil.throwIf(userRegisterRequest.getUserPassword().length() < 8 || userRegisterRequest.getCheckPassword().length() < 8,
                ErrorCode.PARAMS_ERROR, "用户密码过短");
        ThrowUtil.throwIf(!userRegisterRequest.getCheckPassword().equals(userRegisterRequest.getUserPassword()),
                ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        //2. 检查是否和数据库内容重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userRegisterRequest.getUserAccount());
        long count = this.baseMapper.selectCount(queryWrapper);
        ThrowUtil.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "账号重复");
        //3. 密码加密
        String encryptPassword = getEncryptPassword(userRegisterRequest.getUserPassword());
        //4. 插入数据库
        User user = new User();
        user.setUserAccount(userRegisterRequest.getUserAccount());
        user.setUserPassword(encryptPassword);
        user.setUserRole(UserRoleEnum.USER.getValue());
        user.setUserName("无名");
        boolean saveResult = this.save(user);
        ThrowUtil.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "数据库错误, 注册失败");
        return user.getId();
    }

    /**
     *
     * @param userLoginRequest 用户登录信息封装
     * @param request http请求
     * @return 脱敏后的用户信息封装
     */
    @Override
    public LoginUserVO userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request) {
        //1. 校验
        ThrowUtil.throwIf(StrUtil.hasBlank(userLoginRequest.getUserAccount(),
                        userLoginRequest.getUserPassword()),
                ErrorCode.PARAMS_ERROR,
                "用户名或密码为空");
        ThrowUtil.throwIf(userLoginRequest.getUserAccount().length() < 4, ErrorCode.PARAMS_ERROR, "账号错误");
        ThrowUtil.throwIf(userLoginRequest.getUserPassword().length() < 8,
                ErrorCode.PARAMS_ERROR, "用户密码错误");
        //2. 对用户传递的密码进行加密
        String encryptPassword = getEncryptPassword(userLoginRequest.getUserPassword());
        //3. 查询用户是否存在
        QueryWrapper<User> queryWrapper= new QueryWrapper<>();
        queryWrapper.eq("userAccount", userLoginRequest.getUserAccount());
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        if (user == null){
            log.info("user login failed, Account can not match password!");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        //4. 保存用户的登录态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        return getLoginUserVO(user);
    }

    /**
     * 获得脱敏后用户信息
     * @param user 用户
     * @return 脱敏后的信息
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) return null;
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    /**
     * 获取脱敏后的user信息
     * @param user 用户信息
     * @return userVo对象
     */
    @Override
    public UserVO getUserVO(User user) {
        if (user == null) return null;
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * 获取脱敏后的user列表信息
     * @param userList 用户信息
     * @return userVoList对象
     */
    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream()
                .map(this::getUserVO)
                .collect(Collectors.toList());
    }

    /**
     * 密码加密
     *
     * @param password 密码
     * @return 加密后的密码
     */
    @Override
    public String getEncryptPassword(String password) {
        //撒盐
        final String SALT = "F1lower";
        return DigestUtils.md5DigestAsHex((SALT + password).getBytes());
    }

    @Override
    public User getLoginUser(HttpServletRequest httpServletRequest) {
        Object userObj = httpServletRequest.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        ThrowUtil.throwIf(currentUser == null || currentUser.getId() == null, ErrorCode.NO_LOGIN_ERROR);
        //从数据库再次查询, 确保缓存未更新
        Long id = currentUser.getId();
        User flushUser = this.getById(id);
        ThrowUtil.throwIf(flushUser == null, ErrorCode.NO_LOGIN_ERROR);
        return flushUser;
    }

    @Override
    public void userLogout(HttpServletRequest request) {
        //判断用户是否登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        ThrowUtil.throwIf(userObj == null, ErrorCode.OPERATION_ERROR, "未登录");
        //移除用户登录态
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        //判断传递参数是否为空
        ThrowUtil.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");

        //获取所有参数
        Long id = userQueryRequest.getId();
        String userName = userQueryRequest.getUserName();
        String userAccount = userQueryRequest.getUserAccount();
        String userAvatar = userQueryRequest.getUserAvatar();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();

        //封装query
        QueryWrapper<User> userQuery = new QueryWrapper<>();
        userQuery.eq(ObjUtil.isNotNull(id), "id", id);
        userQuery.eq(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);
        userQuery.like(StrUtil.isNotBlank(userName), "userName", userName);
        userQuery.like(StrUtil.isNotBlank(userAvatar), "userAvatar", userAvatar);
        userQuery.like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);
        userQuery.like(StrUtil.isNotBlank(userRole), "userRole", userRole);
        userQuery.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return userQuery;
    }

    /**
     * 判断用户是否具有管理员权限
     * @param user 用户封装信息
     * @return 布尔值
     */
    @Override
    public boolean isAdmin(User user) {
        //判断用户是否为空，是否是管理员
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

}




