package com.itflower.cloudpicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新信息封装类
 */
@Data
public class UserUpdateRequest implements Serializable {

    private static final long serialVersionUID = 7947198670825955201L;
    /**
     * 用户id
     */
    private Long id;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色
     */
    private String userRole;


}
