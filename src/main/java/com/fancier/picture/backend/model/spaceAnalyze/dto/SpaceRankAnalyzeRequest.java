package com.fancier.picture.backend.model.spaceAnalyze.dto;

import lombok.Data;

@Data
public class SpaceRankAnalyzeRequest  {

    /**
     * 排名前 N 的空间
     */
    private Integer topN = 10;
}