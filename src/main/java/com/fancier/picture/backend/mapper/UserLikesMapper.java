package com.fancier.picture.backend.mapper;

import com.fancier.picture.backend.model.picture.UserLikes;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
* @author Fanfan
* @description 针对表【user_likes】的数据库操作Mapper
* @createDate 2025-04-08 10:05:48
* @Entity com.fancier.picture.backend.model.picture.UserLikes
*/
public interface UserLikesMapper extends BaseMapper<UserLikes> {

    void insertIgnoreBatch(List<UserLikes> list);
}




