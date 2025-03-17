package com.fancier.picture.backend.mapper;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fancier.picture.backend.model.user.User;
import com.fancier.picture.backend.model.user.dto.UserPageQuery;

import java.util.Objects;

/**
* @author Fanfan
* @description 针对表【user(用户)】的数据库操作Mapper
* @createDate 2025-03-17 13:05:35
* @Entity com.fancier.picture.backend.model.user.User
*/
public interface UserMapper extends BaseMapper<User> {

    default Page<User> getUsers(UserPageQuery query) {
        int current = query.getCurrent();
        int pageSize = query.getPageSize();


        String sortOrder = query.getSortOrder();
        String sortField = query.getSortField();

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq(Objects.nonNull(query.getId()), "id", query.getId())
                .eq(StrUtil.isBlank(query.getUserAccount()), "user_account", query.getUserAccount())
                .eq(StrUtil.isBlank(query.getUserRole()), "user_role", query.getUserRole())
                .like(StrUtil.isBlank(query.getUserName()), "user_name", query.getUserName())
                .like(StrUtil.isBlank(query.getUserProfile()), "user_profile", query.getUserProfile())
                .orderBy(StrUtil.isBlank(sortField),
                        !Objects.equals(sortOrder, "descend"), sortField)
        ;

        return selectPage(new Page<>(current, pageSize), queryWrapper);
    }
}




