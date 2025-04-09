package com.fancier.picture.backend.model.picture;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @TableName user_likes
 */
@Data
public class UserLikes  {
    private Long id;

    private Long userId;

    private Long pictureId;

    private LocalDateTime createdTime;

}