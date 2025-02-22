package com.itflower.cloudpicturebackend.manager;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.itflower.cloudpicturebackend.config.CosClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    /**
     * 将本地文件上传到 COS
     * @param key 唯一键
     * @param file 文件
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 删除桶内文件
     * @param key 唯一键
     */
    public COSObject getObject(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        return cosClient.getObject(getObjectRequest);
    }

    /**
     * 获取桶内文件
     * @param key 唯一键
     */
    public void deleteObject(String key) {
        cosClient.deleteObject(cosClientConfig.getBucket(), key);
    }

    /**
     * 将本地文件上传到 COS并处理
     * @param key 唯一键
     * @param file 文件
     */
    public PutObjectResult putPictureObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        //对图片进行处理
        PicOperations picOperations = new PicOperations();
        //返回原图信息
        picOperations.setIsPicInfo(1);
        //图片处理规则列表
        List<PicOperations.Rule> ruleList = new ArrayList<>();
        //图片压缩(转成webp)
        String webpKey = FileUtil.mainName(key) + ".webp";
        PicOperations.Rule rule = new PicOperations.Rule();
        rule.setFileId(webpKey);
        rule.setBucket(cosClientConfig.getBucket());
        rule.setRule("imageMogr2/format/webp");
        ruleList.add(rule);
        //缩略图处理, 仅对 > 20KB 的文件进行处理
        if (file.length() > 20 * 1024) {
            String suffix = FileUtil.getSuffix(key);
            if (StrUtil.isBlank(suffix)) {
                suffix = "png";
            }
            //生成新缩略图名称
            String thumbNailKey = FileUtil.mainName(key) + "_thumbnail." + suffix;
            PicOperations.Rule thumbNailRule = new PicOperations.Rule();
            thumbNailRule.setFileId(thumbNailKey);
            thumbNailRule.setBucket(cosClientConfig.getBucket());
            thumbNailRule.setRule(String.format("imageMogr2/thumbnail/%sx%s>", 256, 256));
            ruleList.add(thumbNailRule);
        }
        //构造处理参数
        picOperations.setRules(ruleList);
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }
}
