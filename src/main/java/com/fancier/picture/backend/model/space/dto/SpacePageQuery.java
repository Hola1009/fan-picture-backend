package com.fancier.picture.backend.model.space.dto;

import com.fancier.picture.backend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class SpacePageQuery extends PageRequest {
    /**
     * id
     */
    private Long id;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;

    /**
     * 空间类型：0-私有 1-团队
     */
    private Integer spaceType;
}
