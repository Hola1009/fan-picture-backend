package com.fancier.picture.backend.model.picture.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@Data
public class ReviewPictureRequest {
    @NotNull
    private Long id;
    @Range(min = 1, max = 2, message = "审核状态不存在")
    private Integer reviewStatus;
    @Length(max = 100, message = "审核语句太长")
    private String reviewMessage;
}
