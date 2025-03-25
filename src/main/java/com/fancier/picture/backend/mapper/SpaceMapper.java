package com.fancier.picture.backend.mapper;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fancier.picture.backend.model.space.Space;
import com.fancier.picture.backend.model.space.dto.SpacePageQuery;

/**
* @author Fanfan
* @description 针对表【space(空间)】的数据库操作Mapper
* @createDate 2025-03-18 11:12:49
* @Entity com.fancier.picture.backend.model.space.Space
*/
public interface SpaceMapper extends BaseMapper<Space> {

    default Page<Space> pageQuery(SpacePageQuery pageQuery) {
        QueryWrapper<Space> wrapper = new QueryWrapper<Space>()
                .eq(ObjectUtil.isNotEmpty(pageQuery.getId()), "id", pageQuery.getId())
                .eq(ObjectUtil.isNotEmpty(pageQuery.getUserId()), "user_id", pageQuery.getUserId())
                .eq(ObjectUtil.isNotEmpty(pageQuery.getSpaceLevel()), "is_public", pageQuery.getSpaceLevel())
                .eq(ObjectUtil.isNotEmpty(pageQuery.getSpaceType()), "is_public", pageQuery.getSpaceType())
                .like(StrUtil.isNotBlank(pageQuery.getSpaceName()), "name", pageQuery.getSpaceName());


        return this.selectPage(new Page<>(pageQuery.getCurrent(), pageQuery.getPageSize()), wrapper);
    }
}




