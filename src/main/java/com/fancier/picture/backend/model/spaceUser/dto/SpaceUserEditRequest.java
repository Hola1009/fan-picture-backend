package com.fancier.picture.backend.model.spaceUser.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 编辑空间成员请求
 */
@Data
public class SpaceUserEditRequest implements Serializable {

    /**
     * id
     */
    @NotNull
    private Long id;

    /**
     * 空间角色：viewer/editor/admin
     */
    @Pattern(regexp = "viewer|editor|admin", message = "空间角色必须是 viewer|editor|admin")
    private String spaceRole;

    private static final long serialVersionUID = 1L;
}