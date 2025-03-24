package com.fancier.picture.backend.model.picture.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@Data
public class UpdatePictureRequest {
    @NotNull
    Long id;
    @Length(max = 50, min = 1, message = "名字长度为 1~50")
    String picName;
    @Length(max = 200, min = 1, message = "简介长度为 1~200")
    String introduction;
    String category;
    List<String> tags;
}
