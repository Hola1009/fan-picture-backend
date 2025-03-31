package com.fancier.picture.backend.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fancier.picture.backend.auth.constant.SpaceUserRole;
import com.fancier.picture.backend.common.exception.ErrorCode;
import com.fancier.picture.backend.common.exception.ThrowUtils;
import com.fancier.picture.backend.mapper.SpaceMapper;
import com.fancier.picture.backend.mapper.SpaceUserMapper;
import com.fancier.picture.backend.model.space.Space;
import com.fancier.picture.backend.model.space.constant.SpaceLevelEnum;
import com.fancier.picture.backend.model.space.constant.SpaceTypeEnum;
import com.fancier.picture.backend.model.space.dto.AddSpaceRequest;
import com.fancier.picture.backend.model.space.dto.EditSpaceRequest;
import com.fancier.picture.backend.model.space.dto.SpacePageQuery;
import com.fancier.picture.backend.model.space.dto.UpdateSpaceRequest;
import com.fancier.picture.backend.model.space.vo.SpaceVO;
import com.fancier.picture.backend.model.spaceUser.SpaceUser;
import com.fancier.picture.backend.model.user.User;
import com.fancier.picture.backend.model.user.vo.UserVO;
import com.fancier.picture.backend.service.SpaceService;
import com.fancier.picture.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
* @author Fanfan
* @description 针对表【space(空间)】的数据库操作Service实现
* @createDate 2025-03-18 11:12:49
*/
@Service
@RequiredArgsConstructor
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
    implements SpaceService{

    private final UserService userService;

    private final TransactionTemplate transactionTemplate;

    private final SpaceMapper spaceMapper;

    private final SpaceUserMapper spaceUserMapper;

    @Override
    public Long addSpace(AddSpaceRequest request) {

        Long userid = userService.getLoginUser().getId();

        // 校验空间数量, 公有私有最多各一个
        LambdaQueryChainWrapper<Space> wrapper = lambdaQuery()
                .select(Space::getSpaceType).eq(Space::getUserId, userid);

        List<Space> list = list(wrapper.getWrapper());
        ThrowUtils.throwIf(list.size() >= 2, ErrorCode.PARAM_ERROR, "空间数量已达到上限");

        // 重复类型的空间不允许添加
        if (CollUtil.isNotEmpty(list)) {
            Space oldSpace = list.get(0);
            ThrowUtils.throwIf(oldSpace != null
                            && Objects.equals(oldSpace.getSpaceType(), request.getSpaceType()),
                    ErrorCode.PARAM_ERROR, "该类型空间已存在");

        }
        // 创建空间
        Space space = new Space();
        BeanUtils.copyProperties(request, space);
        fillLevelParameter(space, request.getSpaceLevel());
        space.setUserId(userid);

        // 事务
        transactionTemplate.executeWithoutResult(status -> {
            // 获取分布式锁
            String intern = userid.toString().intern();
            synchronized (intern) {
                save(space);
                // 为第一个用户设置管理员身份
                Long id = space.getId();
                SpaceUser spaceUser = new SpaceUser();
                spaceUser.setSpaceId(id);
                spaceUser.setSpaceRole(SpaceUserRole.ADMIN);
                spaceUser.setUserId(userid);

                spaceUserMapper.insert(spaceUser);
            }

        });
        return space.getId();
    }

    @Override
    public Boolean updateSpace(UpdateSpaceRequest request) {
        validateUpdateParameter(request.getSpaceName(), request.getSpaceLevel());
        Space space = new Space();
        BeanUtils.copyProperties(request, space);
        if (request.getSpaceLevel() != null) {
            fillLevelParameter(space, request.getSpaceLevel());
        }

        return updateById(space);
    }

    @Override
    public Page<Space> pageQuery(SpacePageQuery pageQuery) {
        return spaceMapper.pageQuery(pageQuery);
    }

    @Override
    public Page<SpaceVO> pageQueryVO(SpacePageQuery pageQuery) {
        Page<Space> page = pageQuery(pageQuery);
        List<Space> records = page.getRecords();
        List<Long> userIds = records.stream()
                .map(Space::getUserId).collect(Collectors.toList());

        List<UserVO> userVOS = userService.listVOByIds(userIds);

        Map<Long, UserVO> userIdVoMap = userVOS.stream().collect(Collectors.toMap(UserVO::getId, Function.identity()));

        List<SpaceVO> VOS = records.stream().map(s -> {
            SpaceVO spaceVO = new SpaceVO();
            BeanUtils.copyProperties(s, spaceVO);
            spaceVO.setUser(userIdVoMap.get(s.getUserId()));
            return spaceVO;
        }).collect(Collectors.toList());

        Page<SpaceVO> pageVO = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());

        pageVO.setRecords(VOS);

        return pageVO;
    }

    @Override
    public Boolean edit(EditSpaceRequest request) {
        // 参数校验
        Long spaceId = request.getId();
        Space oldSpace = this.getById(spaceId);

        User user = userService.getById(request.getUserId());
        ThrowUtils.throwIf(user == null, ErrorCode.PARAM_ERROR, "用户 id 非法");

        ThrowUtils.throwIf(oldSpace == null, ErrorCode.PARAM_ERROR, "空间 id 非法");
        validateUpdateParameter(request.getSpaceName(), request.getSpaceLevel());

        ThrowUtils.throwIf(SpaceTypeEnum.of(request.getSpaceType()) == null,
                ErrorCode.PARAM_ERROR, "空间类型非法");

        // 属性拷贝
        Space space = new Space();
        BeanUtils.copyProperties(request, space);

        // 更新
        return updateById(space);
    }


    private void fillLevelParameter(Space space, Integer level) {
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.of(level);
        ThrowUtils.throwIf(spaceLevelEnum == null, ErrorCode.PARAM_ERROR, "空间等级错误");
        space.setMaxCount(spaceLevelEnum.getMaxCount());
        space.setMaxSize(spaceLevelEnum.getMaxSize());
    }

    private void validateUpdateParameter(String spaceName, Integer spaceLevel) {
        if (spaceName != null) {
            int length = spaceName.length();
            ThrowUtils.throwIf(length < 2 || length > 20,
                    ErrorCode.PARAM_ERROR, "空间名长度应在 1 ~ 20 之间");
        }

        if (spaceLevel != null) {
            ThrowUtils.throwIf(SpaceLevelEnum.of(spaceLevel) == null,
                    ErrorCode.PARAM_ERROR, "space level 不合法");
        }
    }

}




