package com.fancier.picture.backend.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.fancier.picture.backend.auth.constant.KitType;
import com.fancier.picture.backend.auth.constant.SpacePermission;
import com.fancier.picture.backend.model.picture.dto.UploadPictureRequest;
import com.fancier.picture.backend.model.picture.vo.UploadPictureResult;
import com.fancier.picture.backend.service.PictureService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@RestController
@RequiredArgsConstructor
@RequestMapping("/picture")
public class PictureController {

    private final PictureService pictureService;

    @PostMapping("/upload")
    @SaCheckPermission(type = KitType.SPACE, value = SpacePermission.PICTURE_UPLOAD)
    public UploadPictureResult upload(@RequestPart("file") MultipartFile multipartFile,
                                      @RequestBody UploadPictureRequest request) {
        return pictureService.uploadPicture(multipartFile, request);
    }

}
