package com.fancier.picture.backend.model.spaceUser;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * @TableName space_user
 */
@Data
public class SpaceUser implements Serializable {
    private Long id;

    private Long spaceId;

    private Long userId;

    private String spaceRole;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private static final long serialVersionUID = 1L;
}