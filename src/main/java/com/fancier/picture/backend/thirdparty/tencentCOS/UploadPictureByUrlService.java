package com.fancier.picture.backend.thirdparty.tencentCOS;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import com.fancier.picture.backend.common.exception.BusinessException;
import com.fancier.picture.backend.common.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URLConnection;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@Service
public class UploadPictureByUrlService extends UploadPictureServiceTemplate {
    @Override
    protected String getOriginalFileName(Object inputSource) {
        String url = (String) inputSource;
        String contentType = URLConnection.guessContentTypeFromName(url);
        if (contentType == null) {
            return null;
        }
        String mainName = FileUtil.mainName(url);
        if (contentType.startsWith("image/jpeg")) {
            return mainName + ".jpg";
        } else if (contentType.startsWith("image/png")) {
            return mainName + ".png";
        } else if (contentType.startsWith("image/gif")) {
            return mainName + ".gif";
        }
        throw new BusinessException(ErrorCode.PARAM_ERROR, "不支持的文件类型");
    }

    @Override
    protected void processFile(Object inputSource, File file) {
        HttpUtil.downloadFile((String) inputSource, file);
    }
}
