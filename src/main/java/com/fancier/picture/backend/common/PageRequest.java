package com.fancier.picture.backend.common;

import lombok.Data;

/**
 * 通用的分页请求类
 * @author <a href="https://github.com/hola1009">fancier</a>
 */
@Data
public class PageRequest {

    /**
     * 当前页号
     */
    private int current = 1;

    /**
     * 页面大小
     */
    private int pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField = "create_time";

    /**
     * 排序顺序（默认升序）
     */
    private String sortOrder = "descend";
}