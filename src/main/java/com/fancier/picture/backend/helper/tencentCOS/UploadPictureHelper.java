package com.fancier.picture.backend.helper.tencentCOS;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.fancier.picture.backend.common.exception.ErrorCode;
import com.fancier.picture.backend.common.exception.ThrowUtils;
import com.fancier.picture.backend.model.picture.vo.UploadPictureResult;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIUploadResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@Component
@RequiredArgsConstructor
public class UploadPictureHelper {

    private final TencentCOSConfigure tencentCOSConfigure;
    private final COSClient cosClient;

    public UploadPictureResult uploadFile(MultipartFile file, String uploadPathPrefix) {
        // 生成上传路劲

        String fileName = file.getOriginalFilename();
        ThrowUtils.throwIf(StrUtil.isBlankIfStr(fileName), ErrorCode.PARAM_ERROR, "文件名不能为空");

        String suffix = FileUtil.getSuffix(fileName);
        String uploadPath = String.format("%s/%s.%s",
                uploadPathPrefix, UUID.randomUUID() + DateUtil.formatDate(new Date()), suffix);

        File tempFile = null;
        try {
            tempFile = File.createTempFile(uploadPath, null);
            file.transferTo(tempFile);

            String bucket = tencentCOSConfigure.getBucket();
            // 上传文件
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, uploadPath, tempFile);

            // 设置返回文件的规则
            PicOperations picOperations = new PicOperations();
            picOperations.setIsPicInfo(1);
            putObjectRequest.setPicOperations(picOperations);

            // 获取响应
            PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);

            // 构建返回实体
            return buildUploadPicResult(putObjectResult, uploadPath, tempFile.length(),fileName);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            tempFile.delete();
        }
    }

    public UploadPictureResult buildUploadPicResult(PutObjectResult putObjectResult, String uploadPath, long size, String fileName) {



        CIUploadResult ciUploadResult = putObjectResult.getCiUploadResult();
        ImageInfo imageInfo = ciUploadResult.getOriginalInfo().getImageInfo();


        UploadPictureResult res = new UploadPictureResult();

        String url = tencentCOSConfigure.getHost() + "/" + uploadPath;
        res.setPicName(FileUtil.mainName(fileName));
        res.setUrl(url);
        res.setPicFormat(imageInfo.getFormat());
        res.setPicWidth(imageInfo.getWidth());
        res.setPicHeight(imageInfo.getHeight());
        res.setPicScale(NumberUtil
                .round(imageInfo.getWidth() * 1.0 / imageInfo.getHeight(), 2)
                .doubleValue());
        res.setPicSize(size);
        res.setPicColor(imageInfo.getAve());

        return res;
    }
}
