package com.fancier.picture.backend.model.space;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @TableName space
 */
@Data
public class Space {
    private Long id;

    private String spaceName;

    private Integer spaceLevel;

    private Long maxSize;

    private Long maxCount;

    private Long totalSize;

    private Long totalCount;

    private Long userId;

    private LocalDateTime createTime;

    private LocalDateTime editTime;

    private LocalDateTime updateTime;

    private Integer isDelete;

    private Integer spaceType;

}