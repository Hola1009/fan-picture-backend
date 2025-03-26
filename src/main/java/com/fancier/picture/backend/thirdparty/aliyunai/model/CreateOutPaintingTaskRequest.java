package com.fancier.picture.backend.thirdparty.aliyunai.model;

import cn.hutool.core.annotation.Alias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@Data
public class CreateOutPaintingTaskRequest {
    private String model = "image-out-painting";
    private Input input;
    private Parameters parameters;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Input {
        @Alias("image_url")
        private String imageUrl;
    }

    @Getter
    @Setter
    public static class Parameters {
        @JsonProperty("xScale")
        @Alias("x_scale")
        private Double xScale;
        @Alias("y_scale")
        @JsonProperty("yScale")
        private Double yScale;
        @Alias("best_quality")
        private Boolean bestQuality;
        @Alias("limit_image_size")
        private Boolean limitImageSize;
        @Alias("angle")
        private Integer angle;
        @Alias("left_offset")
        private Integer leftOffset;
        @Alias("right_offset")
        private Integer rightOffset;
        @Alias("output_ratio")
        private String outputRatio;
    }
}
