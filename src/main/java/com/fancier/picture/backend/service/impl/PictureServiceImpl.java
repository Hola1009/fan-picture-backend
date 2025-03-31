package com.fancier.picture.backend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fancier.picture.backend.auth.constant.UserRole;
import com.fancier.picture.backend.common.exception.BusinessException;
import com.fancier.picture.backend.common.exception.ErrorCode;
import com.fancier.picture.backend.common.exception.ThrowUtils;
import com.fancier.picture.backend.mapper.PictureMapper;
import com.fancier.picture.backend.mapper.SpaceMapper;
import com.fancier.picture.backend.model.picture.Picture;
import com.fancier.picture.backend.model.picture.constant.ReviewType;
import com.fancier.picture.backend.model.picture.dto.*;
import com.fancier.picture.backend.model.picture.vo.PictureTagCategory;
import com.fancier.picture.backend.model.picture.vo.PictureVO;
import com.fancier.picture.backend.model.space.Space;
import com.fancier.picture.backend.model.user.vo.LoginUserVO;
import com.fancier.picture.backend.model.user.vo.UserVO;
import com.fancier.picture.backend.service.PictureService;
import com.fancier.picture.backend.thirdparty.aliyunai.AliYunAiApi;
import com.fancier.picture.backend.thirdparty.aliyunai.model.CreateOutPaintingTaskResponse;
import com.fancier.picture.backend.thirdparty.imageSearch.ImageSearchApiFacade;
import com.fancier.picture.backend.thirdparty.imageSearch.model.ImageSearchResult;
import com.fancier.picture.backend.thirdparty.tencentCOS.UploadPictureByFileService;
import com.fancier.picture.backend.thirdparty.tencentCOS.UploadPictureByUrlService;
import com.fancier.picture.backend.thirdparty.tencentCOS.model.UploadPictureResult;
import com.fancier.picture.backend.util.ColorSimilarUtils;
import com.fancier.picture.backend.util.JsonFileParserUtil;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.awt.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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

    private final SpaceMapper spaceMapper;

    private final PictureMapper pictureMapper;

    private final StringRedisTemplate stringRedisTemplate;

    private final AliYunAiApi aliYunAiApi;

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
            LambdaQueryWrapper<Space> wrapper = new LambdaQueryWrapper<Space>().select(Space::getId)
                    .eq(Space::getId, spaceId);
            Space space = spaceMapper.selectOne(wrapper);
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
        fillReviewStatus(picture);
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
        BeanUtils.copyProperties(request, picture);
        validateAndFillParameter(request.getTags(), request.getCategory(), picture);

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
        // 普通用户只能看到过审的内容
        pageQuery.setReviewStatus(1);
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
            return JSONUtil.toBean(json, Page.class, true);
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
        BeanUtils.copyProperties(request, picture);
        validateAndFillParameter(request.getTags(), request.getCategory(), picture);
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

    @Override
    public Integer batchUpload(BatchUploadPictureRequest request) throws IOException {
        // 获取页面的图片标签
        String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1", request.getSearchText());
        Document document = Jsoup.connect(fetchUrl).get();
        Element div = document.getElementsByClass("dg_b isvctrl").first();
        if (ObjUtil.isEmpty(div)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取元素失败");
        }
        Elements imgElements = Objects.requireNonNull(div).select("img.ming");

        if (StrUtil.isBlank(request.getNamePrefix())) {
            request.setNamePrefix(request.getSearchText());
        }

        Integer count = 0;
        for (Element imgElement : imgElements) {
            String originalUrl = imgElement.attr("src");
            if (StrUtil.isBlank(originalUrl)) {
                continue;
            }
            String url = originalUrl;

            int i = originalUrl.indexOf("?");
            if (i > -1) {
                url = originalUrl.substring(0, i);
            }

            UploadPictureRequest uploadPictureRequest = new UploadPictureRequest();
            uploadPictureRequest.setFileUrl(url);

            String mainName = FileUtil.mainName(url);
            uploadPictureRequest.setPicName(mainName + "-" + count);

            uploadPicture(url, uploadPictureRequest);

            if ((++count).equals(request.getCount())) {
                break;
            }
        }

        return count;
    }

    @Override
    @Transactional
    public Boolean batchEdit(BatchEditPictureRequest request) {
        List<Long> pictureIdList = request.getPictureIdList();
        List<Picture> oldPictures = this.listByIds(pictureIdList);

        Long spaceId = request.getSpaceId();

        Space space = spaceMapper.selectById(spaceId);

        ThrowUtils.throwIf(space == null, ErrorCode.PARAM_ERROR, "空间 id 不存在");

        List<Picture> pictures = oldPictures.stream().map(p -> {
            Picture picture = new Picture();
            validateAndFillParameter(request.getTags(), request.getCategory(), picture);
            return picture;
        }).collect(Collectors.toList());

        fillPictureWithNameRule(pictures, request.getNameRule());

        return this.updateBatchById(pictures);
    }

    @Override
    public List<ImageSearchResult> searchWithPicture(SearchPictureByPictureRequest request) {
        Picture byId = this.getById(request.getPictureId());

        return ImageSearchApiFacade.searchImage(byId.getUrl(), 0);
    }

    @Override
    public List<PictureVO> searchByColor(SearchPictureByColorRequest request) {

        List<Picture> pictures = this.lambdaQuery()
                .eq(Picture::getSpaceId, request.getSpaceId())
                .list();

        Color targetColor = Color.decode(request.getPicColor());

        return pictures.stream().sorted(Comparator.comparingDouble(p -> {
            String hexColor = p.getPicColor();
            if (StrUtil.isBlank(hexColor)) {
                return Double.MAX_VALUE;
            }
            Color pictureColor = Color.decode(hexColor);
            // 进行倒叙排序
            return -ColorSimilarUtils
                    .calculateSimilarity(targetColor, pictureColor);
        })).limit(12).map(p -> {
            PictureVO pictureVO = new PictureVO();
            BeanUtils.copyProperties(p, pictureVO);
            return pictureVO;
        }).collect(Collectors.toList());
    }

    @Override
    public CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest request) {
        Long pictureId = request.getPictureId();
        Picture byId = this.getById(pictureId);
        ThrowUtils.throwIf(byId == null, ErrorCode.PARAM_ERROR, "需要修改的图片不存在");

        return aliYunAiApi.createOutPaintingTask(byId.getUrl(), request.getParameters());
    }


    private void validateAndFillParameter(List<String> tags, String category, Picture picture) {
        ThrowUtils.throwIf(CollUtil.isNotEmpty(tags) && !new HashSet<>(tagList).containsAll(tags),
                ErrorCode.PARAM_ERROR, "部分标签非法");
        ThrowUtils.throwIf(category != null && !categoryList.contains(category),
                ErrorCode.PARAM_ERROR, "分类非法");

        String jsonTagStr = JSONUtil.toJsonStr(tags);
        picture.setTags(jsonTagStr);
        fillReviewStatus(picture);
    }

    private void fillReviewStatus(Picture picture) {
        LoginUserVO loginUser = userService.getLoginUser();
        if (UserRole.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            picture.setReviewerId(loginUser.getId());
            picture.setReviewMessage("自动过审");
            picture.setReviewStatus(ReviewType.PASS.getValue());
            picture.setReviewTime(LocalDateTime.now());
        } else {
            picture.setReviewStatus(ReviewType.REVIEWING.getValue());
        }
    }

    private void fillPictureWithNameRule(List<Picture> pictureList, String nameRule) {
        if (StrUtil.isBlank(nameRule) || CollUtil.isEmpty(pictureList)) {
            return;
        }
        long count = 1;
        try {
            for (Picture picture : pictureList) {
                String pictureName = nameRule.replaceAll("\\{序号}", String.valueOf(count++));
                picture.setPicName(pictureName);
            }
        } catch (Exception e) {
            log.error("名称解析错误", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "名称解析错误");
        }
    }

}




