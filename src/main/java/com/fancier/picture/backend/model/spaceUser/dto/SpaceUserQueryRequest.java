package com.fancier.picture.backend.model.spaceUser.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SpaceUserQueryRequest {

    /**
     * ID
     */
    private Long id;

    /**
     * 空间 ID
     */
    @NotNull
    private Long spaceId;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 空间角色：viewer/editor/admin
     */
    private String spaceRole;
}