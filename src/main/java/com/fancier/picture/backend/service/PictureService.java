package com.fancier.picture.backend.service;

import com.fancier.picture.backend.model.picture.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fancier.picture.backend.model.picture.dto.UploadPictureRequest;
import com.fancier.picture.backend.model.picture.vo.UploadPictureResult;
import org.springframework.web.multipart.MultipartFile;

/**
* @author Fanfan
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-03-17 12:54:14
*/
public interface PictureService extends IService<Picture> {



    UploadPictureResult uploadPicture(MultipartFile multipartFile, UploadPictureRequest request);
}
