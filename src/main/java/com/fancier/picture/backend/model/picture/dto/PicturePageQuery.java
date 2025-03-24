package com.fancier.picture.backend.model.picture.dto;

import com.fancier.picture.backend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class PicturePageQuery extends PageRequest {

    private Long id;

    private String picName;

    private String introduction;

    private String category;

    private List<String> tags;

    private Long picSize;

    private Integer picWidth;

    private Integer picHeight;

    private Double picScale;

    private String picFormat;

    private String searchText;

    private Long userId;

    private Integer reviewStatus;

    private String reviewMessage;

    private Long reviewerId;

    private LocalDateTime reviewTime;

    private Long spaceId;

    private boolean nullSpaceId;

    private LocalDateTime startEditTime;

    private LocalDateTime endEditTime;
}
