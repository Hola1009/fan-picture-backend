package com.fancier.picture.backend.mapper;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fancier.picture.backend.model.picture.Picture;
import com.fancier.picture.backend.model.picture.dto.PicturePageQuery;

import java.util.List;

/**
* @author Fanfan
* @description 针对表【picture(图片)】的数据库操作Mapper
* @createDate 2025-03-17 12:54:14
* @Entity com.fancier.picture.backend.model.picture.Picture
*/
public interface PictureMapper extends BaseMapper<Picture> {

    default Page<Picture> pageQuery(PicturePageQuery pageQuery) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<Picture>()
                .eq(ObjectUtil.isNotEmpty(pageQuery.getId()), "id", pageQuery.getId())
                .eq(ObjectUtil.isNotEmpty(pageQuery.getUserId()), "user_id", pageQuery.getUserId())
                .eq(ObjectUtil.isNotEmpty(pageQuery.getSpaceId()), "space_id", pageQuery.getSpaceId())
                .and(StrUtil.isNotBlank(pageQuery.getSearchText()), wrapper ->
                        wrapper.like("pic_name", pageQuery.getSearchText()).or()
                        .like("introduction", pageQuery.getSearchText()))
                .isNull(pageQuery.isNullSpaceId(), "space_id")
                .like(StrUtil.isNotBlank(pageQuery.getPicName()), "pic_name", pageQuery.getPicName())
                .like(StrUtil.isNotBlank(pageQuery.getIntroduction()), "introduction", pageQuery.getIntroduction())
                .like(StrUtil.isNotBlank(pageQuery.getReviewMessage()), "reviewMessage", pageQuery.getReviewMessage())
                .eq(StrUtil.isNotBlank(pageQuery.getIntroduction()), "category", pageQuery.getCategory())
                .eq(ObjectUtil.isNotEmpty(pageQuery.getPicSize()), "pic_size", pageQuery.getPicSize())
                .eq(ObjectUtil.isNotEmpty(pageQuery.getPicHeight()), "pic_height", pageQuery.getPicHeight())
                .eq(ObjectUtil.isNotEmpty(pageQuery.getPicWidth()), "pic_width", pageQuery.getPicWidth())
                .eq(ObjectUtil.isNotEmpty(pageQuery.getPicScale()), "pic_scale", pageQuery.getPicScale())
                .eq(StrUtil.isNotBlank(pageQuery.getPicFormat()), "pic_format", pageQuery.getPicFormat())
                .eq(ObjectUtil.isNotEmpty(pageQuery.getReviewStatus()), "review_status", pageQuery.getReviewStatus())
                .eq(ObjUtil.isNotEmpty(pageQuery.getReviewerId()), "reviewerId", pageQuery.getReviewerId())
                .ge(ObjUtil.isNotEmpty(pageQuery.getStartEditTime()), "editTime", pageQuery.getStartEditTime())
                .lt(ObjUtil.isNotEmpty(pageQuery.getEndEditTime()), "editTime", pageQuery.getEndEditTime());

        List<String> tags = pageQuery.getTags();
        if (CollUtil.isNotEmpty(tags)) {
            /* and (tag like "%\"Java\"%" and like "%\"Python\"%") */
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(pageQuery.getSortField()), pageQuery.getSortOrder().equals("ascend"), pageQuery.getSortField());

        return this.selectPage(new Page<>(pageQuery.getCurrent(), pageQuery.getPageSize()), queryWrapper);
    }
}




