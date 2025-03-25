package com.fancier.picture.backend.model.space.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@AllArgsConstructor
@Getter
public enum SpaceLevelEnum {
    COMMON("普通版", 0, 100, 100L * 1024 * 1024),
    PROFESSIONAL("专业版", 1, 1000, 1000L * 1024 * 1024),
    FLAGSHIP("旗舰版", 2, 10000, 10000L * 1024 * 1024),
    ;
    private final String text;
    private final Integer value;
    private final long maxCount;
    private final long maxSize;

    public static SpaceLevelEnum of(Integer value) {
        for (SpaceLevelEnum spaceLevelEnum : values()) {
            if (spaceLevelEnum.getValue().equals(value)) {
                return spaceLevelEnum;
            }
        }
        return null;
    }
}
