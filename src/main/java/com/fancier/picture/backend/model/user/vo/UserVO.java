package com.fancier.picture.backend.model.user.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@Data
public class UserVO {
    private Long id;

    private String userAccount;

    private String userName;

    private String userAvatar;

    private String userProfile;

    private String userRole;

    private LocalDateTime createTime;

    private LocalDateTime vipExpireTime;

    private String vipCode;

    private Long vipNumber;
}
