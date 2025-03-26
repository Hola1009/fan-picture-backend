package com.fancier.picture.backend.model.picture.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SearchPictureByPictureRequest {

    /**
     * 图片 id
     */
    @NotNull
    private Long pictureId;
}