package com.fancier.picture.backend.model.spaceAnalyze.vo;

import lombok.Data;

@Data
public class SpaceUsageAnalyzeResponse {

    private Long usedSize;

    private Long maxSize;

    private Double sizeUsageRatio;

    private Long usedCount;

    private Long maxCount;

    private Double countUsageRatio;

}