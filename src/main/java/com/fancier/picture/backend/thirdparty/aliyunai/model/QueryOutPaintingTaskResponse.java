package com.fancier.picture.backend.thirdparty.aliyunai.model;

import cn.hutool.core.annotation.Alias;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@Data
public class QueryOutPaintingTaskResponse {
    @Alias("request_id")
    private String requestId;

    private QueryOutPaintingTaskResponse.Output output;

    @Getter
    @Setter
    public static class Output {
        @Alias("task_id")
        private String taskId;

        @Alias("task_status")
        private String taskStatus;

        @Alias("submit_time")
        private LocalDateTime submitTime;

        @Alias("scheduled_time")
        private LocalDateTime scheduledTime;

        @Alias("end_time")
        private LocalDateTime endTime;

        @Alias("output_image_url")
        private String outputImageUrl;

        @Alias("task_metrics")
        private TaskMetrics taskMetrics;

        private String code;

        private String message;
    }

    @Getter
    @Setter
    public static class TaskMetrics {
        private Integer total;
        private Integer succeeded;
        private Integer failed;
    }
}
