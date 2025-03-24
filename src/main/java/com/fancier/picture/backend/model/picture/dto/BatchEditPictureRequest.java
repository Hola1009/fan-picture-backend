package com.fancier.picture.backend.model.picture.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@Data
public class BatchEditPictureRequest {

    @NotEmpty
    private List<Long> pictureIdList;
    private Long spaceId;
    private String category;
    private List<String> tags;
    private String nameRule;
}
