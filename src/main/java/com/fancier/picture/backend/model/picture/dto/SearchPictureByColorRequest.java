package com.fancier.picture.backend.model.picture.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SearchPictureByColorRequest {

    /**
     * 图片主色调
     */
    @NotNull
    private String picColor;

    /**
     * 空间 id
     */
    @NotNull
    private Long spaceId;
}