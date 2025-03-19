package com.fancier.picture.backend.thirdparty.tencentCOS;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@Service
public class UploadPictureByFileService extends UploadPictureServiceTemplate {

    @Override
    protected String getOriginalFileName(Object inputSource) {
        MultipartFile file = (MultipartFile) inputSource;
        return file.getOriginalFilename();
    }

    @Override
    protected void processFile(Object inputSource, File file) throws IOException {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        multipartFile.transferTo(file);
    }
}
