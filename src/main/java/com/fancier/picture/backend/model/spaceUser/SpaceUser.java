package com.fancier.picture.backend.model.spaceUser;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @TableName space_user
 */
@Data
public class SpaceUser {
    private Long id;

    private Long spaceId;

    private Long userId;

    private String spaceRole;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}