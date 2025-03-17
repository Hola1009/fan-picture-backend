package com.fancier.picture.backend.model.user.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@Data
public class LoginUserVO {
    private Long id;

    private String userAccount;

    private String userName;

    private String userAvatar;

    private String userProfile;

    private String userRole;

    private LocalDateTime editTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
