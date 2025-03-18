package com.fancier.picture.backend.model.picture.dto;

import lombok.Data;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@Data
public class UploadPictureRequest {
    Long id;
    String fileUrl;
    String picName;
    Long spaceId;
}
