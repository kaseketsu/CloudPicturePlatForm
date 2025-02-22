package com.itflower.cloudpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itflower.cloudpicturebackend.model.dto.space.SpaceAddRequest;
import com.itflower.cloudpicturebackend.model.dto.space.SpaceQueryRequest;
import com.itflower.cloudpicturebackend.model.entity.Space;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itflower.cloudpicturebackend.model.entity.User;
import com.itflower.cloudpicturebackend.model.vo.SpaceVO;


import javax.servlet.http.HttpServletRequest;

/**
 * @author 75574
 * @description 针对表【space(空间)】的数据库操作Service
 * @createDate 2025-01-18 10:20:30
 */
public interface SpaceService extends IService<Space> {


    /**
     * 创建空间
     *
     * @param spaceAddRequest
     * @param loginUser
     * @return
     */
    long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);

    /**
     * 判断空间信息是否合法
     *
     * @param space 空间信息封装类
     * @param add 判断是否为创建时校验
     */
    void validSpace(Space space, boolean add);

    /**
     * 空间查询操作
     *
     * @param request 空间查询
     * @return 空间查询SQL语句
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest request);

    /**
     * 获取空间脱敏视图
     *
     * @param spaces  空间实体类
     * @param request http请求
     * @return 空间脱敏视图
     */
    SpaceVO getspaceVO(Space spaces, HttpServletRequest request);

    /**
     * 分页获取空间脱敏封装
     *
     * @param spacesPage 空间分页封装
     * @param request    http请求
     * @return 分页空间脱敏封装
     */
    Page<SpaceVO> getspaceVOPage(Page<Space> spacesPage, HttpServletRequest request);

    /**
     * 根据空间级别自动分配容量
     * @param space 控件对象
     */
    void fillSpaceByLevel(Space space);

}
