package com.fancier.picture.backend.thirdparty.aliyunai.model;

import cn.hutool.core.annotation.Alias;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@Data
public class CreateOutPaintingTaskResponse {

    @Alias("request_id")
    private String requestId;

    private Output output;

    private String code;

    private String message;

    @Getter
    @Setter
    public static class Output {
        @Alias("task_id")
        private String taskId;

        /**
         * <li>PENDING：任务排队中</li>
         * <li>RUNNING：任务处理中</li>
         * <li>SUSPENDED：任务挂起</li>
         * <li>SUCCEEDED：任务执行成功</li>
         * <li>FAILED：任务执行失败</li>
         * <li>UNKNOWN：任务不存在或状态未知</li>
         */
        @Alias("task_status")
        private String taskStatus;
    }
}











