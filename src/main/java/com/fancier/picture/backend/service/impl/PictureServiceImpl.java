package com.fancier.picture.backend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fancier.picture.backend.common.exception.ErrorCode;
import com.fancier.picture.backend.common.exception.ThrowUtils;
import com.fancier.picture.backend.mapper.PictureMapper;
import com.fancier.picture.backend.model.picture.Picture;
import com.fancier.picture.backend.model.picture.constant.ReviewType;
import com.fancier.picture.backend.model.picture.dto.PicturePageQuery;
import com.fancier.picture.backend.model.picture.dto.ReviewPictureRequest;
import com.fancier.picture.backend.model.picture.dto.UpdatePictureRequest;
import com.fancier.picture.backend.model.picture.dto.UploadPictureRequest;
import com.fancier.picture.backend.model.picture.vo.PictureTagCategory;
import com.fancier.picture.backend.model.picture.vo.PictureVO;
import com.fancier.picture.backend.model.space.Space;
import com.fancier.picture.backend.model.user.vo.UserVO;
import com.fancier.picture.backend.service.PictureService;
import com.fancier.picture.backend.service.SpaceService;
import com.fancier.picture.backend.thirdparty.tencentCOS.UploadPictureByFileService;
import com.fancier.picture.backend.thirdparty.tencentCOS.UploadPictureByUrlService;
import com.fancier.picture.backend.thirdparty.tencentCOS.model.UploadPictureResult;
import com.fancier.picture.backend.util.JsonFileParserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author Fanfan
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2025-03-17 12:54:14
*/
@Service
@RequiredArgsConstructor
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService{

    private final UploadPictureByFileService uploadPictureByFileService;

    private final UploadPictureByUrlService uploadPictureByUrlService;

    private final UserServiceImpl userService;

    private final SpaceService spaceService;

    private final PictureMapper pictureMapper;

    private final StringRedisTemplate stringRedisTemplate;

    private static final List<String> tagList;
    private static final List<String> categoryList;

    private static final String PAGE_QUERY_PREFIX = "fanPicture:voPageQueryByCache:";

    static {
        tagList = JsonFileParserUtil.parse2ListFormResource(String.class, "biz/tagList.json");
        categoryList = JsonFileParserUtil.parse2ListFormResource(String.class, "biz/categoryList.json");
    }

    /**
     * 该接口为以下服务共用
     *  1. 上传图片到公共图库
     *  2. 上传图片到个人图库
     *  3. 根据更新图片 url
     * 如果是更新的话优先更新
     * 然后再判断是公共服务还是
     */
    @Override
    public PictureVO uploadPicture(Object inputSource, UploadPictureRequest request) {
        Long spaceId = request.getSpaceId();
        Long pictureId = request.getId();
        Long userId = userService.getLoginUser().getId();
        Picture oldPicture;

        if (pictureId != null) { // 更新图片 url
            oldPicture = getById(pictureId);
            ThrowUtils.throwIf(oldPicture == null, ErrorCode.PARAM_ERROR, "图片不存在");
            // 防止 spaceId 误传
            spaceId = oldPicture.getSpaceId();
            // 如果 pictureId 存在，则说明是更新操做
            // 这一步是预见了管理员操作其他用户图片
            userId = oldPicture.getUserId();
        } else if (spaceId != null) { // 上传图片到指定空间, 需要保证空间存在
            Space space = spaceService.lambdaQuery().select(Space::getId)
                    .eq(Space::getId, spaceId).one();
            ThrowUtils.throwIf(space == null, ErrorCode.PARAM_ERROR, "空间不存在");
        }

        // 上传图片, 区分公共图库和其他图库
        String uploadPathPrefix = spaceId == null ? "public" : "space/" + spaceId;
        UploadPictureResult uploadPictureResult;
        if (inputSource instanceof String) {
            uploadPictureResult = uploadPictureByUrlService.uploadFile(inputSource, uploadPathPrefix);
        } else {
            uploadPictureResult = uploadPictureByFileService.uploadFile(inputSource, uploadPathPrefix);
        }

        Picture picture = new Picture();
        BeanUtils.copyProperties(uploadPictureResult, picture);

        // 通用更新操作和插入操作, 不用写那么多 if 了
        picture.setSpaceId(spaceId);
        picture.setId(pictureId);
        picture.setUserId(userId);

        saveOrUpdate(picture);

        PictureVO pictureVO = new PictureVO();
        BeanUtils.copyProperties(picture, pictureVO);
        autoFillReviewStatus(picture);
        return pictureVO;
    }

    @Override
    public Boolean delete(Long id) {
        return removeById(id);
    }

    @Override
    public Boolean updatePicture(UpdatePictureRequest request) {
        // 参数校验 & 属性拷贝
        Picture picture = new Picture();
        validateAndFillParameter(request, picture);
        autoFillReviewStatus(picture);
        return updateById(picture);
    }

    @Override
    public PictureVO getVOById(Long id) {
        Picture picture = getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAM_ERROR, "图片不存在");

        PictureVO pictureVO = new PictureVO();
        BeanUtils.copyProperties(picture, pictureVO);

        Long userId = picture.getUserId();
        UserVO userVO = userService.getUserVO(userId);

        pictureVO.setUser(userVO);

        return pictureVO;
    }

    @Override
    public Page<Picture> pageQuery(PicturePageQuery pageQuery) {
        return pictureMapper.pageQuery(pageQuery);
    }

    @Override
    public Page<PictureVO> voPageQuery(PicturePageQuery pageQuery) {
        Page<Picture> picturePage = pictureMapper.pageQuery(pageQuery);
        List<Picture> records = picturePage.getRecords();

        // do 转 vo
        List<PictureVO> vos = records.stream().map(p -> {
            PictureVO pictureVO = new PictureVO();
            BeanUtils.copyProperties(p, pictureVO);
            return pictureVO;
        }).collect(Collectors.toList());

        Page<PictureVO> res = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());

        res.setRecords(vos);

        return res;
    }

    @Override
    public Page<PictureVO> voPageQueryByCache(PicturePageQuery pageQuery) {
        String str = pageQuery.toString();
        String hashKey = DigestUtils.md5DigestAsHex(str.getBytes());
        String key = PAGE_QUERY_PREFIX + hashKey;
        ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();

        // 1. 先查 redis 缓存
        String json = operations.get(key);

        if (StrUtil.isNotBlank(json)) {
            Page<PictureVO> res = JSONUtil.toBean(json, Page.class);
            return res;
        }

        // 2. 没查到查数据库, 并将结果放入 redis 缓存
        Page<PictureVO> pictureVOPage = voPageQuery(pageQuery);

        String jsonStr = JSONUtil.toJsonStr(pictureVOPage);

        operations.set(key, jsonStr);

        return pictureVOPage;
    }

    @Override
    public Boolean edit(UpdatePictureRequest request) {
        Picture byId = getById(request.getId());
        ThrowUtils.throwIf(byId == null, ErrorCode.PARAM_ERROR, "图片 id 不存在");

        // 参数校验 属性拷贝
        Picture picture = new Picture();
        validateAndFillParameter(request, picture);
        autoFillReviewStatus(picture);
        return updateById(picture);
    }

    @Override
    public PictureTagCategory listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        pictureTagCategory.setCategoryList(categoryList);
        pictureTagCategory.setTagList(tagList);
        return pictureTagCategory;
    }

    @Override
    public Boolean review(ReviewPictureRequest request) {
        Picture byId = getById(request.getId());
        ThrowUtils.throwIf(byId == null, ErrorCode.PARAM_ERROR, "需要修改的图片不存在");

        Picture picture = new Picture();
        BeanUtils.copyProperties(request, picture);
        picture.setReviewerId(userService.getLoginUser().getId());

        return updateById(picture);
    }


    private void validateAndFillParameter(UpdatePictureRequest request, Picture picture) {
        List<String> tags = request.getTags();
        String category = request.getCategory();

        ThrowUtils.throwIf(CollUtil.isNotEmpty(tags) && !new HashSet<>(tagList).containsAll(tags),
                ErrorCode.PARAM_ERROR, "部分标签非法");
        ThrowUtils.throwIf(category != null && !categoryList.contains(category),
                ErrorCode.PARAM_ERROR, "分类非法");

        BeanUtils.copyProperties(request, picture);
        String jsonTagStr = JSONUtil.toJsonStr(tags);
        picture.setTags(jsonTagStr);
    }

    private void autoFillReviewStatus(Picture picture) {
        Boolean isAdmin = userService.isAdmin();
        if (isAdmin) {
            picture.setReviewStatus(ReviewType.REVIEWING.getValue());
        } else {
            picture.setReviewStatus(ReviewType.PASS.getValue());
        }
    }

}




