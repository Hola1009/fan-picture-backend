package com.fancier.picture.backend.thirdparty.tencentCOS;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@Service
public class UploadPictureByUrlService extends UploadPictureServiceTemplate {
    @Override
    protected String getOriginalFileName(Object inputSource) {
        String url = (String) inputSource;
        return FileUtil.mainName(url).substring(0, 50) + FileUtil.getSuffix(url);
    }

    @Override
    protected void processFile(Object inputSource, File file) {
        HttpUtil.downloadFile((String) inputSource, file);
    }
}
