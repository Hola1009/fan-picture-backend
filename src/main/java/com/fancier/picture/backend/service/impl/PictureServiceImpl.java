package com.fancier.picture.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fancier.picture.backend.helper.tencentCOS.UploadPictureHelper;
import com.fancier.picture.backend.mapper.PictureMapper;
import com.fancier.picture.backend.model.picture.Picture;
import com.fancier.picture.backend.model.picture.dto.UploadPictureRequest;
import com.fancier.picture.backend.model.picture.vo.UploadPictureResult;
import com.fancier.picture.backend.service.PictureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
* @author Fanfan
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2025-03-17 12:54:14
*/
@Service
@RequiredArgsConstructor
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService{

    private final UploadPictureHelper uploadPictureHelper;

    @Override
    public UploadPictureResult uploadPicture(MultipartFile multipartFile, UploadPictureRequest request) {

        return null;
    }
}




