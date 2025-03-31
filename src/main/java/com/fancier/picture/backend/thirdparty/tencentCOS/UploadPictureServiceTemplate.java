package com.fancier.picture.backend.thirdparty.tencentCOS;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.fancier.picture.backend.common.exception.ErrorCode;
import com.fancier.picture.backend.common.exception.ThrowUtils;
import com.fancier.picture.backend.thirdparty.tencentCOS.model.UploadPictureResult;
import com.fancier.picture.backend.util.ColorTransformUtils;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.CIUploadResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/


public abstract class UploadPictureServiceTemplate {

    @Resource
    private CosManager cosManager;

    @Resource
    private TencentCOSConfigure tencentCOSConfigure;

    protected abstract String getOriginalFileName(Object inputSource);

    protected abstract void processFile(Object inputSource, File file) throws IOException;

    public UploadPictureResult uploadFile(Object inputSource, String uploadPathPrefix) {
        // 生成上传路径
        String fileName = getOriginalFileName(inputSource);

        ThrowUtils.throwIf(StrUtil.isBlankIfStr(fileName), ErrorCode.PARAM_ERROR, "文件名不能为空");
        String suffix = FileUtil.getSuffix(fileName);
        String keyName = RandomUtil.randomString(16) + DateUtil.formatDate(new Date());
        String uploadPath = String.format("%s/%s.%s", uploadPathPrefix, keyName, suffix);

        File tempFile = null;
        try {
            tempFile = File.createTempFile(uploadPath, null);
            processFile(inputSource, tempFile);

            // 获取响应
            PutObjectResult putObjectResult = cosManager
                    .uploadPicture(keyName, suffix, uploadPath, tempFile);

            // 构建返回实体
            return buildUploadPicResult(putObjectResult, uploadPath, tempFile.length(), fileName);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }
    }


    private UploadPictureResult buildUploadPicResult(PutObjectResult putObjectResult, String uploadPath, long size, String fileName) {
        // 空值检查
        if (putObjectResult == null || putObjectResult.getCiUploadResult() == null
                || putObjectResult.getCiUploadResult().getOriginalInfo() == null
                || putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo() == null) {
            return null;
        }

        CIUploadResult ciUploadResult = putObjectResult.getCiUploadResult();
        ImageInfo imageInfo = ciUploadResult.getOriginalInfo().getImageInfo();
        List<CIObject> objectList = ciUploadResult.getProcessResults().getObjectList();
        UploadPictureResult res = new UploadPictureResult();

        if (CollUtil.isNotEmpty(objectList)) {
            CIObject compressPic = objectList.get(0);
            CIObject thumbnail = objectList.get(1);
            res.setUrl(tencentCOSConfigure.getHost() + "/" + compressPic.getKey());
            res.setPicName(FileUtil.mainName(fileName));
            setCommonImageInfo(res, compressPic.getWidth(), compressPic.getHeight(), imageInfo.getAve(), compressPic.getFormat(), Long.valueOf(compressPic.getSize()));
            res.setThumbnailUrl(tencentCOSConfigure.getHost() + "/" + thumbnail.getKey());
        } else {
            String url = tencentCOSConfigure.getHost() + "/" + uploadPath;
            res.setUrl(url);
            res.setPicName(FileUtil.mainName(fileName));
            setCommonImageInfo(res, imageInfo.getWidth(), imageInfo.getHeight(), imageInfo.getAve(), imageInfo.getFormat(), size);
        }

        return res;
    }

    private void setCommonImageInfo(UploadPictureResult res, Integer width, Integer height, String picColor, String picFormat, Long picSize) {
        res.setPicWidth(width);
        res.setPicHeight(height);
        res.setPicScale(NumberUtil.round(width * 1.0 / height, 2).doubleValue());
        res.setPicColor(ColorTransformUtils.getStandardColor(picColor));
        res.setPicFormat(picFormat);
        res.setPicSize(picSize);
    }
}
