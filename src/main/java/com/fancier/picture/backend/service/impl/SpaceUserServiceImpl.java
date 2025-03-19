package com.fancier.picture.backend.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fancier.picture.backend.mapper.SpaceUserMapper;
import com.fancier.picture.backend.model.spaceUser.SpaceUser;
import com.fancier.picture.backend.service.SpaceUserService;
import org.springframework.stereotype.Service;

/**
* @author Fanfan
* @description 针对表【space_user(空间用户关联)】的数据库操作Service实现
* @createDate 2025-03-18 23:28:11
*/
@Service
public class SpaceUserServiceImpl extends ServiceImpl<SpaceUserMapper, SpaceUser>
    implements SpaceUserService{

    @Override
    public String getSpaceRole(Long userId, Long spaceId) {

        LambdaQueryChainWrapper<SpaceUser> eq = this.lambdaQuery().select(SpaceUser::getSpaceRole)
                .eq(SpaceUser::getUserId, userId)
                .eq(SpaceUser::getSpaceId, spaceId);

        SpaceUser one = this.getOne(eq);

        return one.getSpaceRole();
    }
}




