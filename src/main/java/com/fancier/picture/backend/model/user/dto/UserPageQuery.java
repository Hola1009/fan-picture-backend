package com.fancier.picture.backend.model.user.dto;

import com.fancier.picture.backend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class UserPageQuery extends PageRequest {
    private Long id;

    private String userAccount;

    private String userName;

    private String userProfile;

    private String userRole;
}
