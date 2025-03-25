package com.fancier.picture.backend.model.spaceAnalyze.dto;

import lombok.Data;

/**
 * 通用空间分析请求
 */
@Data
public class SpaceAnalyzeRequest {

    /**
     * 空间 ID
     */
    private Long spaceId;

    /**
     * 是否查询公共图库
     */
    private boolean queryPublic = true;

    /**
     * 全空间分析
     */
    private boolean queryAll;
}
