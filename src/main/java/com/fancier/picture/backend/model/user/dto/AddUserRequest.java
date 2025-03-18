package com.fancier.picture.backend.model.user.dto;

import com.fancier.picture.backend.auth.constant.UserRole;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@Data
public class AddUserRequest {

    @Length(min = 4, max = 11, message = "用户账号长度在4-11")
    @NotNull
    private String userAccount;

    @Length(min = 1, max = 11, message = "用户账号长度在1-11")
    private String userName;

    @Length(min = 1, max = 50, message = "用户账号长度在1-50")
    private String userProfile;

    private final String userPassword = "12345678";

    private String userAvatar;

    @Pattern(regexp = "admin|user|vip", message = "角色值必须是 user、vip 或 admin 之一")
    private String userRole = UserRole.USER_ROLE;
}
