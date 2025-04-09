package com.fancier.picture.backend.model.picture.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@Data
@AllArgsConstructor
public class LikeInfoVO {
    private Boolean isLike;
    private Integer likesCount;
}
