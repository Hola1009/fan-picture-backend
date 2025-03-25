package com.fancier.picture.backend.model.space.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@Data
public class AddSpaceRequest {
    /**
     * 空间名称
     */
    @Length(min = 2, max = 20, message = "空间名长度应为2 - 20")
    @NotBlank
    private String spaceName;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    @Range(min = 0, max = 2, message = "空间级别不存在")
    @NotNull
    private Integer spaceLevel;

    /**
     * 空间类型：0-私有 1-团队
     */
    @Range(min = 0, max = 1, message = "空间类型不存在")
    @NotNull
    private Integer spaceType;
}

