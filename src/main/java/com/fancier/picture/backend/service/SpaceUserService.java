package com.fancier.picture.backend.service;

import com.fancier.picture.backend.model.spaceUser.SpaceUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fancier.picture.backend.model.spaceUser.dto.AddSpaceUserRequest;
import com.fancier.picture.backend.model.spaceUser.dto.SpaceUserEditRequest;
import com.fancier.picture.backend.model.spaceUser.dto.SpaceUserQueryRequest;
import com.fancier.picture.backend.model.spaceUser.vo.SpaceUserVO;

import java.util.List;

/**
* @author Fanfan
* @description 针对表【space_user(空间用户关联)】的数据库操作Service
* @createDate 2025-03-18 23:28:11
*/
public interface SpaceUserService extends IService<SpaceUser> {

    String getSpaceRole(Long userId, Long spaceId);

    Boolean add(AddSpaceUserRequest request);

    SpaceUser getSpaceUser(SpaceUserQueryRequest spaceUserQueryRequest);

    List<SpaceUserVO> getSpaceUserVOList(SpaceUserQueryRequest request);

    Boolean edit(SpaceUserEditRequest spaceUserEditRequest);

    List<SpaceUserVO> getMy();
}
