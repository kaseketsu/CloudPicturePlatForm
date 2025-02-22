package com.itflower.cloudpicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户添加信息封装类
 */
@Data
public class UserAddRequest implements Serializable {

    private static final long serialVersionUID = 5240332094554959802L;
    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简洁
     */
    private String userProfile;

    /**
     * 用户角色
     */
    private String userRole;

}
