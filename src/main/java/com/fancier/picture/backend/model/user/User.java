package com.fancier.picture.backend.model.user;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * @TableName user
 */
@Data
public class User implements Serializable {
    private Long id;

    private String userAccount;

    private String userPassword;

    private String userName;

    private String userAvatar;

    private String userProfile;

    private String userRole;

    private LocalDateTime editTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer isDelete;

    private LocalDateTime vipExpireTime;

    private String vipCode;

    private Long vipNumber;

    private static final long serialVersionUID = 1L;
}