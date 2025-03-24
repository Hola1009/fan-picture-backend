package com.fancier.picture.backend.model.picture.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@Data
public class BatchUploadPictureRequest {
    @NotBlank(message = "查询词条不能为空")
    private String searchText;
    @Min(value = 1, message = "批量获取数量不能小于 1")
    private Integer count;
    private String namePrefix;
}
