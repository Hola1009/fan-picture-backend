package com.fancier.picture.backend.model.picture.vo;

import lombok.Data;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@Data
public class UploadPictureResult {

    private String url;

    private String picName;

    private Long picSize;

    private Integer picWidth;

    private Integer picHeight;

    private Double picScale;

    private String picFormat;

    private String thumbnailUrl;

    private String picColor;
}
