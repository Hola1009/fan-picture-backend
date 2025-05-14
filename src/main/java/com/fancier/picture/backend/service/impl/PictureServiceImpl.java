package com.fancier.picture.backend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
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
import com.fancier.picture.backend.model.picture.vo.LikeInfoVO;
import com.fancier.picture.backend.model.picture.vo.PictureTagCategory;
import com.fancier.picture.backend.model.picture.vo.PictureVO;
import com.fancier.picture.backend.model.space.Space;
import com.fancier.picture.backend.model.user.vo.UserVO;
import com.fancier.picture.backend.service.PictureService;
import com.fancier.picture.backend.thirdparty.aliyunai.AliYunAiApi;
import com.fancier.picture.backend.thirdparty.aliyunai.model.CreateOutPaintingTaskResponse;
import com.fancier.picture.backend.thirdparty.imageSearch.ImageSearchApiFacade;
import com.fancier.picture.backend.thirdparty.imageSearch.model.ImageSearchResult;
import com.fancier.picture.backend.thirdparty.tencentCOS.CosManager;
import com.fancier.picture.backend.thirdparty.tencentCOS.UploadPictureByFileService;
import com.fancier.picture.backend.thirdparty.tencentCOS.UploadPictureByUrlService;
import com.fancier.picture.backend.thirdparty.tencentCOS.model.UploadPictureResult;
import com.fancier.picture.backend.util.ColorSimilarUtils;
import com.fancier.picture.backend.util.FileParserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.DigestUtils;

import java.awt.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
* @author Fanfan
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2025-03-17 12:54:14
*/
@Service
@Slf4j
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

    private final TransactionTemplate transactionTemplate;

    private final CosManager cosManager;

    private final ThreadPoolTaskExecutor myThreadPool;


    private static final List<String> tagList;
    private static final List<String> categoryList;

    private static final String PAGE_QUERY_PREFIX = "fan_picture:picture:vo_page_query_by_cache:";

    private static final String PICTURE_LIKE_PREFIX = "fan_picture:picture:picture_likes:";

    private static final String PICTURE_LIKE_COUNT_PREFIX = "fan_picture:picture:picture_like_count:";
    private static final String PICTURE_LIKE_TIME_PREFIX = "fan_picture:picture:picture_like_time:";

    private static final String OUT_PAINTING_TASK_PREFIX = "fan_picture:picture:out_painting_task:";

    static {
        tagList = FileParserUtil.parseJsonFile2ListFormResource(String.class, "biz/tagList.json");
        categoryList = FileParserUtil.parseJsonFile2ListFormResource(String.class, "biz/categoryList.json");
    }

    /**
     * 该接口为以下服务共用
     *  1. 上传图片到公共图库
     *  2. 上传图片到个人图库
     *  3. 根据更新图片 url
     * 如果是更新的话优先更新
     * 然后再判断是公共服务还是
     * 1001 这个 spaceId 并不存在, 图片关联它只是标识这是一张用户头像
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
        } else if (spaceId != null && spaceId != 1001) { // 上传图片到指定空间, 需要保证空间存在
            LambdaQueryWrapper<Space> wrapper = new LambdaQueryWrapper<Space>().select(Space::getId)
                    .eq(Space::getId, spaceId);
            Space space = spaceMapper.selectOne(wrapper);
            ThrowUtils.throwIf(space == null, ErrorCode.PARAM_ERROR, "空间不存在");
        }

        // 上传图片, 区分公共图库和其他图库
        String uploadPathPrefix = spaceId == null || spaceId == 1001 ? "public" : "space/" + spaceId;
        UploadPictureResult uploadPictureResult;
        if (inputSource instanceof String) {
            uploadPictureResult = uploadPictureByUrlService.uploadFile(inputSource, uploadPathPrefix);
        } else {
            uploadPictureResult = uploadPictureByFileService.uploadFile(inputSource, uploadPathPrefix);
        }

        Picture picture = new Picture();
        BeanUtils.copyProperties(uploadPictureResult, picture);

        // 通用更新操作和插入操作, 不用写那么多 if 了
        if (request.getPicName() != null) {
            picture.setPicName(request.getPicName());
        }
        picture.setSpaceId(spaceId);
        picture.setId(pictureId);
        picture.setUserId(userId);


        Long finalSpaceId = spaceId;
        transactionTemplate.execute(status -> {
            try {
                fillReviewStatus(picture);
                // 保存图片
                saveOrUpdate(picture);
                // 操作2
                if (finalSpaceId != null && finalSpaceId != 1001) {
                    UpdateWrapper<Space> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.setSql("total_count = total_count + 1")
                            .setSql("total_size = total_size + " + picture.getPicSize())
                            .eq("id", finalSpaceId);
                    spaceMapper.update(updateWrapper);
                }
                return status;
            } catch (BusinessException e) {
                status.setRollbackOnly();
                throw e;
            }
        });


        PictureVO pictureVO = new PictureVO();
        BeanUtils.copyProperties(picture, pictureVO);

        return pictureVO;
    }

    @Override
    public Boolean delete(Long id) {
        transactionTemplate.execute(status -> {
            try {

                Picture picture = getById(id);
                ThrowUtils.throwIf(picture == null, ErrorCode.PARAM_ERROR, "图片不存在");
                Long spaceId = picture.getSpaceId();
                removeById(id);
                // 操作2
                if (spaceId != null) {
                    UpdateWrapper<Space> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.setSql("total_count = total_count - 1")
                            .setSql("total_size = total_size - " + picture.getPicSize())
                            .eq("id", spaceId);
                    spaceMapper.update(updateWrapper);
                }
                cosManager.deletePicture(picture.getUrl());
                return status;
            } catch (BusinessException e) {
                status.setRollbackOnly();
                throw e;
            }
        });
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

        List<String> tags = JSONUtil.toList(picture.getTags(), String.class);
        pictureVO.setTags(tags);


        Long userId = picture.getUserId();
        UserVO userVO = userService.getUserVO(userId);

        pictureVO.setUser(userVO);

        UpdateWrapper<Picture> pictureUpdateWrapper = new UpdateWrapper<>();
        pictureUpdateWrapper.setSql("views = views + 1")
                .eq("id", id);

        pictureMapper.update(pictureUpdateWrapper);

        return pictureVO;
    }

    @Override
    public Page<Picture> pageQuery(PicturePageQuery pageQuery) {
        return pictureMapper.pageQuery(pageQuery);
    }

    @Override
    public Page<PictureVO> voPageQuery(PicturePageQuery pageQuery) {
        // 普通用户只能看到过审的内容 和 公共图库
        if (pageQuery.getSpaceId() == null) {
            pageQuery.setNullSpaceId(true);
            pageQuery.setReviewStatus(1);
        }

        Page<Picture> picturePage = pictureMapper.pageQuery(pageQuery);
        List<Picture> records = picturePage.getRecords();

        // do 转 vo

        Page<PictureVO> res = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());

        Long userId = userService.getLoginUser().getId();
        // 1. 使用正确的 Pipeline 操作收集结果
        List<Long> pictureIds = records.stream().map(Picture::getId).collect(Collectors.toList());

        // 2. 批量获取点赞状态（正确版）
        List<LikeInfoVO> likeStatus = batchCheckLikeStatus(userId, pictureIds);

        List<PictureVO> vos = IntStream.range(0, records.size()).mapToObj(i -> {
            PictureVO pictureVO = new PictureVO();
            BeanUtils.copyProperties(records.get(i), pictureVO);
            pictureVO.setLike(likeStatus.get(i).getIsLike());
            pictureVO.setLikesCount(likeStatus.get(i).getLikesCount());
            return pictureVO;
        }).collect(Collectors.toList());


        res.setRecords(vos);

        return res;
    }


    public List<LikeInfoVO> batchCheckLikeStatus(Long userId, List<Long> pictureIds) {
        // 1. 使用Pipeline同时查询两种数据，减少网络延迟，提升查询性能
        List<Object> results = stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            // 查询用户点赞状态
            for (Long picId : pictureIds) {
                connection.sIsMember(
                        (PICTURE_LIKE_PREFIX + picId).getBytes(),
                        userId.toString().getBytes()
                );
            }

            // 查询点赞总数
            // 未匹配的项会返回空
            for (Long picId : pictureIds) {
                connection.get(
                        (PICTURE_LIKE_COUNT_PREFIX + picId).getBytes()
                );
            }
            return null;
        });

        // 2. 处理混合结果
        List<LikeInfoVO> resultMap = new ArrayList<>();
        int halfSize = results.size() / 2;

        for (int i = 0; i < halfSize; i++) {
            Boolean isLiked = (Boolean) results.get(i);
            Integer likeCount = results.get(i + halfSize) != null ?
                    Integer.parseInt((String) results.get(i + halfSize)) : 0;

            resultMap.add(new LikeInfoVO(isLiked, likeCount));
        }

        return resultMap;
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
        picture.setReviewTime(LocalDateTime.now());
        return updateById(picture);
    }

    @Override
    public Integer batchUpload(BatchUploadPictureRequest request) throws IOException {
        // 获取页面的图片标签
        String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1", request.getSearchText());
        Document document = Jsoup.connect(fetchUrl).get();
        Element div = document.getElementsByClass("dgControl").first();
        if (ObjUtil.isEmpty(div)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取元素失败");
        }
        Elements imgElements = Objects.requireNonNull(div).select("img.mimg");

        if (StrUtil.isBlank(request.getNamePrefix())) {
            request.setNamePrefix(request.getSearchText());
        }

        Integer count = 0;
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (Element imgElement : imgElements) {
            String originalUrl = imgElement.attr("src");
            if (StrUtil.isBlank(originalUrl)) {
                continue;
            }
            String url;

            int i = originalUrl.indexOf("?");
            if (i > -1) {
                url = originalUrl.substring(0, i);
            } else {
                url = originalUrl;
            }

            UploadPictureRequest uploadPictureRequest = new UploadPictureRequest();
            uploadPictureRequest.setFileUrl(url);

            uploadPictureRequest.setPicName(request.getNamePrefix() + "-" + count);

            CompletableFuture<Void> future = CompletableFuture.runAsync(
                    () -> uploadPicture(url, uploadPictureRequest),
                    myThreadPool
            );
            futures.add(future);

        }

        // 等待所有异步任务完成
        CompletableFuture<Void> allOf = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );

        allOf.join();

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

        String s = stringRedisTemplate.opsForValue().get(OUT_PAINTING_TASK_PREFIX + byId);

        if (StrUtil.isBlank(s)) {
            CreateOutPaintingTaskResponse outPaintingTask = aliYunAiApi.createOutPaintingTask(byId.getUrl(), request.getParameters());
            String jsonStr = JSONUtil.toJsonStr(outPaintingTask);

            stringRedisTemplate.opsForValue().set(OUT_PAINTING_TASK_PREFIX + byId, jsonStr, 9, TimeUnit.SECONDS);

            return outPaintingTask;
        }

        String jsonStr = stringRedisTemplate.opsForValue().get(OUT_PAINTING_TASK_PREFIX + byId);

        return JSONUtil.toBean(jsonStr, CreateOutPaintingTaskResponse.class);
    }

    /**
     * 点赞服务
     */
    @Override
    public void pictureLike(PictureLikeRequest request) {
        Long userId = userService.getLoginUser().getId();
        String key = PICTURE_LIKE_PREFIX + request.getPictureId();
        String userIdStr = userId.toString();
        String likeCountKey = PICTURE_LIKE_COUNT_PREFIX + request.getPictureId();
        String likeTimeKey = PICTURE_LIKE_TIME_PREFIX + request.getPictureId();

        // 查询用户是否已经点赞
        boolean isLiked = Boolean.TRUE.equals(
                stringRedisTemplate.opsForSet().isMember(key, userId.toString())
        );

        // Redis 原子操作
        // 管道（pipeline）可以将多个命令打包在一起发送给Redis服务器，
        // 而不是逐条发送。这样可以减少网络往返的次数，从而提高性能。
        stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            connection.multi(); // 开启事务
            if (!isLiked) {
                // 记录用户点赞关系
                connection.sAdd(key.getBytes(), userIdStr.getBytes());
                // 记录用户点赞时间
                connection.zAdd(likeTimeKey.getBytes(), System.currentTimeMillis() / 1000.0, userIdStr.getBytes());
                // 增加点赞计数
                connection.incr(likeCountKey.getBytes());
            } else {
                // 移除用户点赞关系
                connection.sRem(key.getBytes(), userIdStr.getBytes());
                // 用处用户赞时间
                connection.zRem(likeTimeKey.getBytes(), userIdStr.getBytes());
                // 减少点赞计数
                connection.decr(likeCountKey.getBytes());
            }
            return connection.exec();
        });

//        myThreadPool.execute(() -> transactionTemplate.execute(status -> { // 修改为有返回值的方法
//            if(!isLiked) {
//                UserLikes userLikes = new UserLikes();
//                userLikes.setPictureId(request.getPictureId());
//                userLikes.setUserId(userId);
//                userLikesMapper.insert(userLikes);
//            } else {
//                UpdateWrapper<UserLikes> userLikesUpdateWrapper = new UpdateWrapper<>();
//                userLikesUpdateWrapper.eq("picture_id", request.getPictureId()).eq("user_id", userId);
//                userLikesMapper.delete(userLikesUpdateWrapper);
//            }
//
//            log.info("开始执行更新操作");
//            UpdateWrapper<Picture> picUpdateWrapper = new UpdateWrapper<>();
//            String sql = !isLiked ? "likes_count = likes_count + 1" : "likes_count = likes_count - 1";
//            picUpdateWrapper.setSql(sql).eq("id", request.getPictureId());
//            pictureMapper.update(null, picUpdateWrapper);
//            log.info("执行更新操作结束");
//            return null; // 确保事务提交
//        }));
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
        UserVO loginUser = userService.getLoginUser();
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




