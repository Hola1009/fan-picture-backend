package com.fancier.picture.backend.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fancier.picture.backend.mapper.PictureMapper;
import com.fancier.picture.backend.mapper.UserLikesMapper;
import com.fancier.picture.backend.model.picture.Picture;
import com.fancier.picture.backend.model.picture.UserLikes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@Component
@Slf4j
@RequiredArgsConstructor
public class PictureLikesSyncTask {

    private final StringRedisTemplate stringRedisTemplate;

    private final UserLikesMapper userLikesMapper;

    private final PictureMapper pictureMapper;

    private static final String LAST_SYNC_KEY = "fan_picture:picture:picture_like_last_sync_timestamp";

    private static final String PICTURE_LIKE_PREFIX = "fan_picture:picture:picture_likes:";

    private static final String PICTURE_LIKE_COUNT_PREFIX = "fan_picture:picture:picture_like_count:";

    private static final String PICTURE_LIKE_TIME_PREFIX = "fan_picture:picture:picture_like_time:";

    /**
     * 每5分钟进行一次增量同步并从数据库中删除取消了的点赞
     */
    @Scheduled(fixedRate = 300000)
    public void syncIncrementally() {
        log.info("开始: 增量同步图片用户点赞关系到数据库, 并处理Redis已删除的点赞关系");
        // 1. 获取Redis最后同步时间戳
        long lastSyncTime = getLastSyncTimestamp();

        // 2. 同步新增点赞
        syncNewLikes(lastSyncTime);

        // 3. 同步取消点赞
        syncUnlikes();

        // 5. 同步图片点赞数量
        syncPicLikesCount();

        // 4. 更新同步时间
        updateSyncTimestamp();
        log.info("结束: 增量同步图片用户点赞关系到数据库, 并处理Redis已删除的点赞关系");
    }

    private void syncPicLikesCount() {
        Set<String> keys = stringRedisTemplate.keys(PICTURE_LIKE_COUNT_PREFIX + "*");
        List<Picture> list = Objects.requireNonNull(keys).stream().map(key -> {
            Long pictureId = Long.parseLong(StrUtil.removePrefix(key, PICTURE_LIKE_COUNT_PREFIX));
            Integer likesCount = Integer.parseInt(Objects.requireNonNull(stringRedisTemplate.opsForValue().get(key)));
            Picture picture = new Picture();
            picture.setId(pictureId);
            picture.setLikesCount(likesCount);
            return picture;
        }).collect(Collectors.toList());

        pictureMapper.updateById(list);
    }

    /**
     * 增量同步
     */
    private void syncNewLikes(long sinceTime) {
        // 使用ZSET记录点赞时间戳
        Set<String> newLikeKeys = stringRedisTemplate.keys(PICTURE_LIKE_PREFIX + "*");
        Objects.requireNonNull(newLikeKeys).forEach(key -> {
            long pictureId = Long.parseLong(StrUtil.removePrefix(key, PICTURE_LIKE_PREFIX));

            // 获取新增点赞用户（ZRANGEBYSCORE）
            Set<String> newUserIds = stringRedisTemplate.opsForZSet()
                    .rangeByScore(PICTURE_LIKE_TIME_PREFIX + pictureId, sinceTime, Double.MAX_VALUE);

            List<UserLikes> entityList = Objects.requireNonNull(newUserIds).stream().map(userId -> {
                UserLikes userLikes = new UserLikes();
                userLikes.setPictureId(pictureId);
                userLikes.setUserId(Long.parseLong(userId));
                return userLikes;
            }).collect(Collectors.toList());

            // 批量插入并忽略重复的元素
            if (CollUtil.isNotEmpty(entityList)) {
                userLikesMapper.insertIgnoreBatch(entityList);
            }

        });
    }

    private void syncUnlikes() {
        // 从Redis Set与MySQL比对找出取消的点赞
        Set<String> likeKeys = stringRedisTemplate.keys(PICTURE_LIKE_PREFIX + "*");
        Objects.requireNonNull(likeKeys).forEach(key -> {
            Long pictureId = Long.parseLong(key.substring(PICTURE_LIKE_PREFIX.length()));
            Set<String> members = stringRedisTemplate.opsForSet().members(key);

            Set<Long> redisUserIds = Objects.requireNonNull(members).stream()
                    .map(Long::parseLong).collect(Collectors.toSet());


            QueryWrapper<UserLikes> wrapper = new QueryWrapper<>();
            wrapper.select("user_id")
                    .eq("picture_id", pictureId);

            Set<Long> mysqlUserIds = userLikesMapper.selectList(wrapper).stream()
                    .map(UserLikes::getUserId).collect(Collectors.toSet());


            // 找出MySQL有但Redis没有的记录
            mysqlUserIds.removeAll(redisUserIds);

            if (CollUtil.isNotEmpty(mysqlUserIds)) {

                UpdateWrapper<UserLikes> userLikesUpdateWrapper = new UpdateWrapper<>();
                userLikesUpdateWrapper.eq("picture_id", pictureId)
                        .in("user_id", mysqlUserIds);


                userLikesMapper.delete(userLikesUpdateWrapper);
            }

        });
    }

    // 获取最后同步时间（默认返回24小时前的时间戳）
    private long getLastSyncTimestamp() {
        String timestampStr = stringRedisTemplate.opsForValue().get(LAST_SYNC_KEY);
        return timestampStr != null ?
                Long.parseLong(timestampStr) :
                System.currentTimeMillis() - 86400_000; // 默认24小时前
    }

    // 更新同步时间戳为当前时间
    private void updateSyncTimestamp() {
        stringRedisTemplate.opsForValue().set(
                LAST_SYNC_KEY,
                String.valueOf(System.currentTimeMillis())
        );
    }


}
