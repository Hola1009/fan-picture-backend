package com.fancier.picture.backend.model.picture.dto;


import com.fancier.picture.backend.thirdparty.aliyunai.model.CreateOutPaintingTaskRequest;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 创建扩图任务请求
 */
@Data
public class CreatePictureOutPaintingTaskRequest {

    /**
     * 图片 id
     */
    @NotNull
    private Long pictureId;

    /**
     * 扩图参数
     */
    private CreateOutPaintingTaskRequest.Parameters parameters;
}