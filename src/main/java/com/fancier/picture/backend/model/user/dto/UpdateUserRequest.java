package com.fancier.picture.backend.model.user.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@Data
public class UpdateUserRequest {
    @NotNull(message = "用户ID不能为空")
    private Long id;

    @Length(min = 4, max = 11, message = "用户账号长度在4-11")
    private String userAccount;

    @Length(min = 1, max = 11, message = "用户账号长度在1-11")
    private String userName;

    private String userAvatar;

    @Pattern(regexp = "admin|user|vip", message = "角色值必须是 user、vip 或 admin 之一")
    private String userRole;
}

