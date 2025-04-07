package com.fancier.picture.backend.model.user.dto;

import lombok.Data;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@Data
public class SendValidationCodeRequest {
    private String mailAddress;
}
