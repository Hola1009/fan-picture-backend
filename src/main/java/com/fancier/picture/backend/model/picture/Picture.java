package com.fancier.picture.backend.model.picture;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 图片
 * @TableName picture
 */
@Data
public class Picture {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String url;

    private String picName;

    private String introduction;

    private String category = "无";

    private String tags;

    private Long picSize;

    private Integer picWidth;

    private Integer picHeight;

    private Double picScale;

    private String picFormat;

    private Long userId;

    private LocalDateTime createTime;

    private LocalDateTime editTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDelete;

    private Integer reviewStatus;

    private String reviewMessage;

    private Long reviewerId;

    private LocalDateTime reviewTime;

    private String thumbnailUrl;

    private Long spaceId;

    private String picColor;

    private Integer likesCount;

    private Integer views;
}