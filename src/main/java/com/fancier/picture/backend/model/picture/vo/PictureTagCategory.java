package com.fancier.picture.backend.model.picture.vo;

import lombok.Data;

import java.util.List;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@Data
public class PictureTagCategory {
    /**
     * 标签列表
     */
    private List<String> tagList;

    /**
     * 分类列表
     */
    private List<String> categoryList;
}
