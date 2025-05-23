package com.fancier.picture.backend.thirdparty.aliyunai;

import cn.hutool.http.ContentType;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.fancier.picture.backend.common.exception.BusinessException;
import com.fancier.picture.backend.common.exception.ErrorCode;
import com.fancier.picture.backend.thirdparty.aliyunai.model.CreateOutPaintingTaskRequest;
import com.fancier.picture.backend.thirdparty.aliyunai.model.CreateOutPaintingTaskResponse;
import com.fancier.picture.backend.thirdparty.aliyunai.model.QueryOutPaintingTaskResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@Component
@Slf4j
public class AliYunAiApi {

    @Value("${aliYunAi.apiKey}")
    private String apiKey;

    private static final String CREATE_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/out-painting";

    private static final String QUERY_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/tasks/";


    public CreateOutPaintingTaskResponse createOutPaintingTask(String url, CreateOutPaintingTaskRequest.Parameters parameters) {

        HttpRequest post = HttpRequest.post(CREATE_OUT_PAINTING_TASK_URL);
        post.contentType(ContentType.JSON.getValue());
        post.header("Authorization", "Bearer " + apiKey);
        post.header("X-DashScope-Async", "enable");


        CreateOutPaintingTaskRequest request = new CreateOutPaintingTaskRequest();
        CreateOutPaintingTaskRequest.Input input = new CreateOutPaintingTaskRequest.Input(url);
        request.setInput(input);
        request.setParameters(parameters);
        post.body(JSONUtil.toJsonStr(request));

        try (HttpResponse response = post.execute()) {
            if (!response.isOk()) {
                log.error("请求异常：{}", response.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI 扩图失败");
            }

            String body = response.body();

            CreateOutPaintingTaskResponse res = JSONUtil.toBean(body, CreateOutPaintingTaskResponse.class);

            if (res.getCode() != null) {
                String errorMessage = res.getMessage();
                log.error("请求异常：{}", errorMessage);
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI 扩图失败，" + errorMessage);
            }
            log.info("AI 阔图任务执行成功");
            return res;
        }
    }

    public QueryOutPaintingTaskResponse queryOutPaintingTaskResponse(String taskId) {
        HttpRequest get = HttpRequest.get(QUERY_OUT_PAINTING_TASK_URL + taskId);
        get.header("Authorization", "Bearer " + apiKey);
        try (HttpResponse response = get.execute()) {
            if (!response.isOk()) {
                log.error("请求异常：{}", response.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI 扩图任务查询失败");
            }

            return JSONUtil.toBean(response.body(), QueryOutPaintingTaskResponse.class);
        }
    }
}
