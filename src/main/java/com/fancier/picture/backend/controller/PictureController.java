package com.fancier.picture.backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.fancier.picture.backend.auth.constant.KitType;
import com.fancier.picture.backend.auth.constant.SpacePermission;
import com.fancier.picture.backend.model.picture.dto.UploadPictureRequest;
import com.fancier.picture.backend.model.picture.vo.PictureVO;
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
    @SaCheckLogin(type = KitType.USER)
    @SaCheckPermission(type = KitType.SPACE, value = SpacePermission.PICTURE_VIEW)
    public PictureVO upload(@RequestPart("file") MultipartFile multipartFile,
                            UploadPictureRequest request) {
        return pictureService.uploadPicture(multipartFile, request);
    }

    @PostMapping("/upload/url")
    @SaCheckLogin(type = KitType.USER)
    @SaCheckPermission(type = KitType.SPACE, value = SpacePermission.PICTURE_VIEW)
    public PictureVO uploadByUrl(@RequestBody UploadPictureRequest request) {
        String fileUrl = request.getFileUrl();
        return pictureService.uploadPicture(fileUrl, request);
    }


}
