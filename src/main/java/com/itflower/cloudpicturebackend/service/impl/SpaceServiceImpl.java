package com.itflower.cloudpicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itflower.cloudpicturebackend.exception.ErrorCode;
import com.itflower.cloudpicturebackend.exception.ThrowUtil;
import com.itflower.cloudpicturebackend.model.dto.space.SpaceAddRequest;
import com.itflower.cloudpicturebackend.model.dto.space.SpaceQueryRequest;
import com.itflower.cloudpicturebackend.model.entity.Pictures;
import com.itflower.cloudpicturebackend.model.entity.Space;
import com.itflower.cloudpicturebackend.model.entity.User;
import com.itflower.cloudpicturebackend.model.enums.SpaceLevelEnum;
import com.itflower.cloudpicturebackend.model.vo.PictureVO;
import com.itflower.cloudpicturebackend.model.vo.SpaceVO;
import com.itflower.cloudpicturebackend.model.vo.UserVO;
import com.itflower.cloudpicturebackend.service.SpaceService;
import com.itflower.cloudpicturebackend.mapper.SpaceMapper;
import com.itflower.cloudpicturebackend.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 75574
 * @description 针对表【space(空间)】的数据库操作Service实现
 * @createDate 2025-01-18 10:20:30
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceService {

    @Resource
    private UserService userService;

    @Resource
    private TransactionTemplate transactionTemplate;


    @Override
    public long addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        //填充参数默认值
        Space space = new Space();
        BeanUtil.copyProperties(spaceAddRequest, space);
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        if (ObjUtil.isNull(spaceName)) {
            space.setSpaceName("默认空间");
        }
        if (ObjUtil.isNull(spaceLevel)) {
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        fillSpaceByLevel(space);
        //校验参数
        validSpace(space, true);
        //控制权限, 非管理员只能创建一个空间
        Long id = loginUser.getId();
        ThrowUtil.throwIf(
                space.getSpaceLevel() != SpaceLevelEnum.COMMON.getValue() &&
                        !userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR,
                "无权限创建指定级别的空间"
        );
        //控制同一用户只能创建一个空间
        String lock = String.valueOf(id).intern();
        synchronized (lock) {
            Long executeId = transactionTemplate.execute(status -> {
                //判断是否存在已有空间
                boolean exists = this.lambdaQuery()
                        .eq(Space::getUserId, id)
                        .exists();
                //如果存在, 抛异常
                ThrowUtil.throwIf(exists, ErrorCode.OPERATION_ERROR, "同一个用户只能创建一个空间");
                space.setUserId(loginUser.getId());
                //根据level获取枚举写入space
                SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
                space.setMaxCount(spaceLevelEnum.getMaxCount());
                space.setMaxSize(spaceLevelEnum.getMaxSize());
                //创建
                boolean b = this.save(space);
                ThrowUtil.throwIf(!b, ErrorCode.OPERATION_ERROR, "保存空间到数据库失败");
                //返回新写入的id
                return space.getId();
            });
            return Optional.ofNullable(executeId).orElse(-1L);
        }
    }

    @Override
    public void validSpace(Space space, boolean add) {
        ThrowUtil.throwIf(space == null, ErrorCode.PARAMS_ERROR, "空间信息不能为空");
        //取值
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
        //创建空间时检验
        if (add) {
            ThrowUtil.throwIf(spaceName == null, ErrorCode.PARAMS_ERROR, "空间名称不能为空");
            ThrowUtil.throwIf(ObjUtil.isNull(spaceLevel), ErrorCode.PARAMS_ERROR, "空间级别不能为空");
        }
        //编辑空间时检验
        if (StrUtil.isNotBlank(spaceName)) {
            ThrowUtil.throwIf(spaceName.length() > 30, ErrorCode.PARAMS_ERROR, "空间名称过长");
        }
        if (ObjUtil.isNotNull(spaceLevel)) {
            ThrowUtil.throwIf(ObjUtil.isNull(spaceLevelEnum), ErrorCode.PARAMS_ERROR, "错误的空间级别");
        }
    }

    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest request) {
        //判断传递参数是否为空
        ThrowUtil.throwIf(request == null, ErrorCode.PARAMS_ERROR, "请求参数为空");

        //获取所有参数
        Long id = request.getId();
        String spaceName = request.getSpaceName();
        Integer spaceLevel = request.getSpaceLevel();
        Long userId = request.getUserId();
        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();

        //封装query
        QueryWrapper<Space> spaceQuery = new QueryWrapper<>();

        spaceQuery.eq(ObjUtil.isNotNull(id), "id", id);
        spaceQuery.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        spaceQuery.eq(ObjUtil.isNotEmpty(spaceLevel), "spaceLevel", spaceLevel);
        spaceQuery.like(StrUtil.isNotBlank(spaceName), "spaceName", spaceName);

        //排序
        spaceQuery.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return spaceQuery;
    }

    @Override
    public SpaceVO getspaceVO(Space space, HttpServletRequest request) {
        //转换为VO类
        SpaceVO spaceVO = SpaceVO.ObjToVO(space);
        //关联查询用户信息
        Long userID = space.getUserId();
        if (userID != null && userID > 0) {
            User user = userService.getById(userID);
            UserVO userVO = userService.getUserVO(user);
            spaceVO.setUserVO(userVO);
        }
        return spaceVO;
    }

    @Override
    public Page<SpaceVO> getspaceVOPage(Page<Space> spacesPage, HttpServletRequest request) {
        List<Space> spacesList = spacesPage.getRecords();
        //创新空的PageVO
        Page<SpaceVO> spaceVOPage = new Page<>(spacesPage.getCurrent(), spacesPage.getSize(), spacesPage.getTotal());
        if (CollUtil.isEmpty(spacesList)) {
            return spaceVOPage;
        }
        //将space列表转换为spaceVO列表
        List<SpaceVO> spaceVOList = spacesList.stream().map(SpaceVO::ObjToVO).collect(Collectors.toList());
        //对用户id进行去重
        Set<Long> userIdSet = spacesList.stream().map(Space::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdAndListMap = userService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        //填充spaceVO的userVO
        spaceVOList.forEach(spaceVO -> {
            Long userID = spaceVO.getUserId();
            User user = null;
            if (userIdAndListMap.containsKey(userID)) {
                user = userIdAndListMap.get(userID).get(0);
            }
            spaceVO.setUserVO(userService.getUserVO(user));
        });
        spaceVOPage.setRecords(spaceVOList);
        return spaceVOPage;
    }

    /**
     * 根据空间级别分配容量
     *
     * @param space 控件对象
     */
    @Override
    public void fillSpaceByLevel(Space space) {
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if (ObjUtil.isNotNull(spaceLevelEnum)) {
            long maxCount = spaceLevelEnum.getMaxCount();
            if (space.getMaxCount() == null) {
                space.setMaxCount(maxCount);
            }
            long maxSize = spaceLevelEnum.getMaxSize();
            if (space.getMaxSize() == null) {
                space.setMaxSize(maxSize);
            }
        }
    }
}




