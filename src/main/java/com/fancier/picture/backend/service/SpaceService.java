package com.fancier.picture.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fancier.picture.backend.model.Space;
import com.fancier.picture.backend.model.User;
import com.fancier.picture.backend.model.space.dto.SpaceAddRequest;

/**
* @author Fanfan
* @description 针对表【space(空间)】的数据库操作Service
* @createDate 2025-03-06 10:36:56
*/
public interface SpaceService extends IService<Space> {
    void validSpace(Space space, boolean add);

    void fillSpaceBySpaceLevel(Space space);

    Long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);

}
