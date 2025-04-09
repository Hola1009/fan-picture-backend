package com.fancier.picture.backend.model.picture.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@Data
public class PictureLikeRequest {
    @NotNull(message = "图片id不能为空")
    private Long pictureId;
}
