package com.fancier.picture.backend.thirdparty.tencentCOS;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@Component
public class CosManager {

    @Resource
    private TencentCOSConfigure tencentCOSConfigure;
    @Resource
    private COSClient cosClient;

    public PutObjectResult uploadPicture(String keyName, String suffix, String uploadPath, File file) {

        String bucket = tencentCOSConfigure.getBucket();
        // 构建请求
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, uploadPath, file);

        // 设置返回文件
        PicOperations picOperations = new PicOperations();
        picOperations.setIsPicInfo(1);
        putObjectRequest.setPicOperations(picOperations);

        // 设置图片处理规则
        List<PicOperations.Rule> ruleList = getRules(bucket, keyName, suffix);
        picOperations.setRules(ruleList);

        // 获取响应
        return cosClient.putObject(putObjectRequest);

    }

    private List<PicOperations.Rule> getRules(String bucket, String keyName, String suffix) {
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
}
