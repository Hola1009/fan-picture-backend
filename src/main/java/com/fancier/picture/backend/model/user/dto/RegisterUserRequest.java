package com.fancier.picture.backend.model.user.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;


/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@Data
public class RegisterUserRequest {
    @Length(min = 4, max = 11, message = "用户账号长度在4-11")
    private String userAccount;

    @Length(min = 8, max = 20, message = "用户密码长度在8-20")
    private String userPassword;

    @Length(min = 8, max = 20, message = "确认密码长度不符合")
    private String checkPassword;
    @Email
    private String mailAddress;
}
