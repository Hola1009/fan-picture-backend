package com.fancier.picture.backend.model.spaceAnalyze.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceUserAnalyzeResponse {

    /**
     * 时间区间
     */
    private String period;

    /**
     * 上传数量
     */
    private Long count;
}