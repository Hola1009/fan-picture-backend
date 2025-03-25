package com.fancier.picture.backend.model.spaceUser.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@Data
public class AddSpaceUserRequest {
    @NotNull
    private Long spaceId;

    @NotNull
    private Long userId;

    @Pattern(regexp = "admin|viewer|editor", message = "spaceRole 非法")
    private String spaceRole;
}
