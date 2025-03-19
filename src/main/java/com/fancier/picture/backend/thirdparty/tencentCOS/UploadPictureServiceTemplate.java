package com.fancier.picture.backend.thirdparty.tencentCOS;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.fancier.picture.backend.common.exception.ErrorCode;
import com.fancier.picture.backend.common.exception.ThrowUtils;
import com.fancier.picture.backend.thirdparty.tencentCOS.model.UploadPictureResult;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.CIUploadResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/


public abstract class UploadPictureServiceTemplate {

    @Resource
    private TencentCOSConfigure tencentCOSConfigure;
    @Resource
    private COSClient cosClient;

    protected abstract String getOriginalFileName(Object inputSource);

    protected abstract void processFile(Object inputSource, File file) throws IOException;

    public UploadPictureResult uploadFile(Object inputSource, String uploadPathPrefix) {
        // 生成上传路径
        String fileName = getOriginalFileName(inputSource);

        ThrowUtils.throwIf(StrUtil.isBlankIfStr(fileName), ErrorCode.PARAM_ERROR, "文件名不能为空");
        String suffix = FileUtil.getSuffix(fileName);
        String keyName = UUID.randomUUID() + DateUtil.formatDate(new Date());
        String uploadPath = String.format("%s/%s.%s", uploadPathPrefix, keyName, suffix);

        File tempFile = null;
        try {
            tempFile = File.createTempFile(uploadPath, null);
            processFile(inputSource, tempFile);

            String bucket = tencentCOSConfigure.getBucket();
            // 构建请求
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, uploadPath, tempFile);

            // 设置返回文件
            PicOperations picOperations = new PicOperations();
            picOperations.setIsPicInfo(1);
            putObjectRequest.setPicOperations(picOperations);

            // 设置图片处理规则
            List<PicOperations.Rule> ruleList = getRules(bucket, keyName, suffix);
            picOperations.setRules(ruleList);

            // 获取响应
            PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);

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

    private static List<PicOperations.Rule> getRules(String bucket, String keyName, String suffix) {
        List<PicOperations.Rule> ruleList = new LinkedList<>();

        // 添加生成压缩图规则
        PicOperations.Rule rule1 = new PicOperations.Rule();
        rule1.setBucket(bucket);
        rule1.setFileId(String.format("%s-%s.%s", keyName, "compress", "webp"));
        rule1.setRule("imageMogr2/format/webp");
        ruleList.add(rule1);

        // 添加生成缩略图规则
        PicOperations.Rule rule2 = new PicOperations.Rule();
        rule2.setBucket(bucket);
        rule2.setFileId(String.format("%s-%s.%s", keyName, "thumbnail", suffix));
        rule2.setRule(String.format("imageMogr2/thumbnail/%sx%s>", 256, 256));
        ruleList.add(rule2);
        return ruleList;
    }

    private UploadPictureResult buildUploadPicResult(PutObjectResult putObjectResult, String uploadPath, long size, String fileName) {

        CIUploadResult ciUploadResult = putObjectResult.getCiUploadResult();
        ImageInfo imageInfo = ciUploadResult.getOriginalInfo().getImageInfo();

        List<CIObject> objectList = ciUploadResult.getProcessResults().getObjectList();
        UploadPictureResult res = new UploadPictureResult();

        // 如果有缩略图和压缩图
        if (CollUtil.isNotEmpty(objectList)) {
            CIObject compressPic = objectList.get(0);
            CIObject thumbnail = objectList.get(1);
            res.setUrl(compressPic.getLocation());
            res.setPicName(compressPic.getKey());
            res.setPicSize(Long.valueOf(compressPic.getSize()));
            res.setPicWidth(compressPic.getWidth());
            res.setPicHeight(compressPic.getHeight());
            res.setPicScale(NumberUtil
                    .round(compressPic.getWidth() * 1.0 / compressPic.getHeight(), 2).doubleValue());
            res.setPicFormat(compressPic.getFormat());
            res.setThumbnailUrl(thumbnail.getLocation());
            res.setPicColor(imageInfo.getAve());
            return res;
        }

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
