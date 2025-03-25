package com.fancier.picture.backend.mapper;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fancier.picture.backend.model.spaceUser.SpaceUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fancier.picture.backend.model.spaceUser.dto.SpaceUserQueryRequest;

/**
* @author Fanfan
* @description 针对表【space_user(空间用户关联)】的数据库操作Mapper
* @createDate 2025-03-18 23:28:11
* @Entity com.fancier.picture.backend.model.spaceUser.SpaceUser
*/
public interface SpaceUserMapper extends BaseMapper<SpaceUser> {

    default Wrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest req) {
        QueryWrapper<SpaceUser> wrapper = new QueryWrapper<>();

        wrapper.eq(ObjectUtil.isNotEmpty(req.getId()), "id", req.getId())
        .eq(ObjectUtil.isNotEmpty(req.getSpaceId()), "space_id", req.getSpaceId())
        .eq(ObjectUtil.isNotEmpty(req.getUserId()), "user_id", req.getUserId())
        .eq(StrUtil.isNotBlank(req.getSpaceRole()), "space_role", req.getSpaceRole());

        return wrapper;
    }
}




