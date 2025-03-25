package com.fancier.picture.backend.model.space.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@AllArgsConstructor
@Getter
public enum SpaceTypeEnum {
    PRIVATE("私有空间", 0),
    TEAM("团队空间", 1);

    private final String text;
    private final Integer value;

    public static SpaceTypeEnum of(Integer value) {
        for (SpaceTypeEnum spaceTypeEnum : values()) {
            if (spaceTypeEnum.value.equals(value)) {
                return spaceTypeEnum;
            }
        }
        return null;
    }
}
