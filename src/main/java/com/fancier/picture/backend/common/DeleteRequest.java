package com.fancier.picture.backend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用的删除请求类
 * @author <a href="https://github.com/hola1009">fancier</a>
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}