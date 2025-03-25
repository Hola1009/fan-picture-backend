package com.fancier.picture.backend.model.spaceAnalyze.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceUserAnalyzeRequest extends SpaceAnalyzeRequest {

    /**
     * 用户 ID
     */
    @NotNull
    private Long userId;

    /**
     * 时间维度：day / week / month
     */
    @Pattern(regexp = "day|week|month", message = "时间维度不合法")
    private String timeDimension;
}