package com.fancier.picture.backend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fancier.picture.backend.common.exception.ErrorCode;
import com.fancier.picture.backend.common.exception.ThrowUtils;
import com.fancier.picture.backend.mapper.SpaceMapper;
import com.fancier.picture.backend.mapper.SpaceUserMapper;
import com.fancier.picture.backend.model.space.Space;
import com.fancier.picture.backend.model.space.constant.SpaceTypeEnum;
import com.fancier.picture.backend.model.space.vo.SpaceVO;
import com.fancier.picture.backend.model.spaceUser.SpaceUser;
import com.fancier.picture.backend.model.spaceUser.dto.AddSpaceUserRequest;
import com.fancier.picture.backend.model.spaceUser.dto.SpaceUserEditRequest;
import com.fancier.picture.backend.model.spaceUser.dto.SpaceUserQueryRequest;
import com.fancier.picture.backend.model.spaceUser.vo.SpaceUserVO;
import com.fancier.picture.backend.model.user.User;
import com.fancier.picture.backend.model.user.vo.UserVO;
import com.fancier.picture.backend.service.SpaceUserService;
import com.fancier.picture.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
* @author Fanfan
* @description 针对表【space_user(空间用户关联)】的数据库操作Service实现
* @createDate 2025-03-18 23:28:11
*/
@Service
@RequiredArgsConstructor
public class SpaceUserServiceImpl extends ServiceImpl<SpaceUserMapper, SpaceUser>
    implements SpaceUserService{

    private final SpaceMapper spaceMapper;

    private final UserService userService;

    private final SpaceUserMapper spaceUserMapper;

    @Override
    public String getSpaceRole(Long userId, Long spaceId) {

        LambdaQueryChainWrapper<SpaceUser> eq = this.lambdaQuery().select(SpaceUser::getSpaceRole)
                .eq(SpaceUser::getUserId, userId)
                .eq(SpaceUser::getSpaceId, spaceId);

        SpaceUser one = this.getOne(eq.getWrapper());

        return one.getSpaceRole();
    }

    @Override
    public Boolean add(AddSpaceUserRequest request) {

        User user = userService.getById(request.getUserId());
        ThrowUtils.throwIf(user == null,
                ErrorCode.PARAM_ERROR, "需要添加的用户 id 非法");

        Space space = spaceMapper.selectById(request.getSpaceId());
        ThrowUtils.throwIf(space == null,
                ErrorCode.PARAM_ERROR, "需要关联的空间 id 非法");

        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(request, spaceUser);

        return updateById(spaceUser);

    }

    @Override
    public SpaceUser getSpaceUser(SpaceUserQueryRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        Long spaceId = request.getSpaceId();
        Long userId = request.getUserId();
        ThrowUtils.throwIf(ObjectUtil.hasEmpty(spaceId, userId), ErrorCode.PARAMS_ERROR);
        // 查询数据库
        SpaceUser spaceUser = getOne(spaceUserMapper.getQueryWrapper(request));
        ThrowUtils.throwIf(spaceUser == null, ErrorCode.NOT_FOUND_ERROR);
        return spaceUser;
    }

    @Override
    public List<SpaceUserVO> getSpaceUserVOList(SpaceUserQueryRequest request) {
        return this.list(
                spaceUserMapper.getQueryWrapper(request)
        ).stream().map(o -> {
            SpaceUserVO spaceUserVO = new SpaceUserVO();
            BeanUtils.copyProperties(o, spaceUserVO);
            return spaceUserVO;
        }).collect(Collectors.toList());
    }

    @Override
    public Boolean edit(SpaceUserEditRequest spaceUserEditRequest) {
        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(spaceUserEditRequest, spaceUser);
        return updateById(spaceUser);
    }

    @Override
    public List<SpaceUserVO> getMy() {
        UserVO loginUser = userService.getLoginUser();
        SpaceUserQueryRequest spaceUserQueryRequest = new SpaceUserQueryRequest();
        spaceUserQueryRequest.setUserId(loginUser.getId());
        List<SpaceUser> spaceUserList = list(
                spaceUserMapper.getQueryWrapper(spaceUserQueryRequest)
        );

        List<Long> spaceIdList = spaceUserList.stream().map(SpaceUser::getSpaceId).collect(Collectors.toList());

        if (CollUtil.isEmpty(spaceIdList)) return Collections.emptyList();
        Map<Long, Space> idSpaceMap = spaceMapper.selectByIds(spaceIdList).stream()
                .collect(Collectors.toMap(Space::getId, Function.identity()));


        return spaceUserList.stream().map(o -> {
            SpaceUserVO spaceUserVO = new SpaceUserVO();

            BeanUtils.copyProperties(o, spaceUserVO);
            SpaceVO spaceVO = new SpaceVO();

            BeanUtils.copyProperties(idSpaceMap.get(o.getSpaceId()), spaceVO);
            spaceUserVO.setSpace(spaceVO);
            return spaceUserVO;
        })
        .filter(o -> Objects.equals(o.getSpace().getSpaceType(), SpaceTypeEnum.TEAM.getValue()))
        .collect(Collectors.toList());
    }
}




