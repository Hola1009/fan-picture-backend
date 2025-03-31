package com.fancier.picture.backend.service.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fancier.picture.backend.common.exception.BusinessException;
import com.fancier.picture.backend.common.exception.ErrorCode;
import com.fancier.picture.backend.mapper.PictureMapper;
import com.fancier.picture.backend.mapper.SpaceMapper;
import com.fancier.picture.backend.model.picture.Picture;
import com.fancier.picture.backend.model.space.Space;
import com.fancier.picture.backend.model.spaceAnalyze.constant.SizeRange;
import com.fancier.picture.backend.model.spaceAnalyze.dto.SpaceAnalyzeRequest;
import com.fancier.picture.backend.model.spaceAnalyze.dto.SpaceRankAnalyzeRequest;
import com.fancier.picture.backend.model.spaceAnalyze.dto.SpaceUserAnalyzeRequest;
import com.fancier.picture.backend.model.spaceAnalyze.vo.*;
import com.fancier.picture.backend.service.SpaceAnalyzeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@Service
@RequiredArgsConstructor
public class SpaceAnalyzeServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceAnalyzeService {

    private final PictureMapper pictureMapper;

    private final SpaceMapper spaceMapper;

    @Override
    public SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceAnalyzeRequest request) {

        SpaceUsageAnalyzeResponse res = new SpaceUsageAnalyzeResponse();
        if (request.isQueryAll() || request.isQueryPublic()) {
            QueryWrapper<Picture> wrapper = new QueryWrapper<>();
            wrapper.select("pic_size");
            if (request.isQueryPublic()) {
                wrapper.isNull("space_id");
            }

            List<Picture> pictures = pictureMapper.selectList(wrapper);

            res.setUsedSize(pictures.stream()
                    .mapToLong(Picture::getPicSize).sum());

            res.setUsedCount((long) pictures.size());
        } else {
            Space space = getById(request.getSpaceId());
            res.setMaxCount(space.getMaxCount());
            res.setMaxSize(space.getMaxSize());
            res.setUsedCount(space.getTotalCount());
            res.setUsedSize(space.getTotalSize());
            res.setSizeUsageRatio(NumberUtil
                    .round(space.getTotalSize() * 1.0 / space.getMaxSize() , 2)
                    .doubleValue());
            res.setCountUsageRatio(NumberUtil
                    .round(space.getTotalCount() * 1.0 / space.getMaxCount() , 2)
                    .doubleValue());
        }

        return res;
    }

    @Override
    public List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceAnalyzeRequest request) {

        QueryWrapper<Picture> wrapper = getWrapper(request, "category", "pic_size");

        List<Picture> pictures = pictureMapper.selectList(wrapper);

        // 对查询结果进行封装
        return new ArrayList<>(pictures.stream()
                .collect(Collectors.groupingBy(
                        Picture::getCategory,
                        // 分组后在下游收集器进行封装
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    SpaceCategoryAnalyzeResponse res = new SpaceCategoryAnalyzeResponse();
                                    res.setCategory(list.get(0).getCategory()); // 所有元素category相同
                                    res.setCount((long) list.size());
                                    res.setTotalSize(list.stream().mapToLong(Picture::getPicSize).sum());
                                    return res;
                                }
                        )
                )).values());
    }

    @Override
    public List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceAnalyzeRequest request) {

        QueryWrapper<Picture> wrapper = getWrapper(request, "tags");

        List<Picture> pictures = pictureMapper.selectList(wrapper);


        return pictures.stream()
                .filter(Objects::nonNull)
                .map(Picture::getTags)
                // 经行扁平化
                .flatMap(tags -> JSONUtil.toList(tags, String.class).stream())
                // 按照标签名进行分类统计
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.counting()
                ))
                .entrySet().stream()
                // 排序
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                // 结果封装
                .map(e -> new SpaceTagAnalyzeResponse(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceAnalyzeRequest request) {
        QueryWrapper<Picture> wrapper = getWrapper(request, "pic_size");

        List<Picture> pictures = pictureMapper.selectList(wrapper);

        return Arrays.stream(SizeRange.values())
                .map(range -> new SpaceSizeAnalyzeResponse(
                        range.getRangeName(),
                        pictures.stream()
                                .filter(p -> range.getMatcher().test(p.getPicSize()))
                                .count()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest request) {
        QueryWrapper<Picture> wrapper = new QueryWrapper<>();

        Long userId = request.getUserId();
        wrapper.eq(Objects.nonNull(userId), "user_id", userId);


        String timeDimension = request.getTimeDimension();
        switch (timeDimension) {
            case "day":
                wrapper.select("DATE_FORMAT(create_time, '%Y-%m-%d') as period", "count(*) as count");
                break;
            case "week":
                wrapper.select("YEARWEEK(create_time) as period", "count(*) as count");
                break;
            case "month":
                wrapper.select("DATE_FORMAT(create_time, '%Y-%m') as period", "count(*) as count");
                break;
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的时间维度");
        }


        wrapper.groupBy("period").orderByAsc("period");


        List<Map<String, Object>> maps = pictureMapper.selectMaps(wrapper);

        return maps.stream().
                map(map -> new SpaceUserAnalyzeResponse(map.get("period").toString(), (Long) map.get("count")))
                .collect(Collectors.toList());
    }

    @Override
    public List<Space> getSpaceRankAnalyze(SpaceRankAnalyzeRequest request) {
        QueryWrapper<Space> wrapper = new QueryWrapper<>();

        wrapper.select("id", "space_name", "user_id", "total_size")
                .orderByDesc("total_size")
                .last("limit " + request.getTopN());

        return spaceMapper.selectList(wrapper);
    }

    private QueryWrapper<Picture> getWrapper(SpaceAnalyzeRequest request, String... columns) {
        QueryWrapper<Picture> wrapper = new QueryWrapper<>();

        wrapper.select(columns)
                .isNull(request.isQueryPublic(), "space_id")
                .eq(request.getSpaceId() != null,
                        "space_id", request.getSpaceId());
        return wrapper;
    }

}
