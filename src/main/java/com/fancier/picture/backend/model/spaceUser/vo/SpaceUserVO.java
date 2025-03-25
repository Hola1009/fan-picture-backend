package com.fancier.picture.backend.model.spaceUser.vo;

import com.fancier.picture.backend.model.space.vo.SpaceVO;
import com.fancier.picture.backend.model.user.vo.UserVO;
import lombok.Data;

import java.util.Date;

/**
 * 空间成员响应类
 */
@Data
public class SpaceUserVO {

    /**
     * id
     */
    private Long id;

    /**
     * 空间 id
     */
    private Long spaceId;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 空间角色：viewer/editor/admin
     */
    private String spaceRole;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 用户信息
     */
    private UserVO user;

    /**
     * 空间信息
     */
    private SpaceVO space;
}