package com.fancier.picture.backend.model.space.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@Data
public class EditSpaceRequest {
    @NotNull
    private Long id;

    private Long userId;

    private String spaceName;

    private Integer spaceLevel;

    private Integer spaceType;
}
