package com.fancier.picture.backend.model.picture.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@AllArgsConstructor
@Getter
public enum ReviewType {
    REVIEWING("待审核", 0),
    PASS("通过", 1),
    REJECT("拒绝", 2),

    ;
    private final String name;
    private final Integer value;

}
