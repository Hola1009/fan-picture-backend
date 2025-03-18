package com.fancier.picture.backend.auth.model;

import lombok.Data;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@Data
public class SpaceAuthContext {
    Long id;
    Long pictureId;
    Long userId;
    Long spaceId;
    Long spaceUserId;
    String servletPath;
}
