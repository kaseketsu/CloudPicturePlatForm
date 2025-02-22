package com.itflower.cloudpicturebackend.manager.upload;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.itflower.cloudpicturebackend.config.CosClientConfig;
import com.itflower.cloudpicturebackend.exception.BusinessException;
import com.itflower.cloudpicturebackend.exception.ErrorCode;
import com.itflower.cloudpicturebackend.manager.CosManager;
import com.itflower.cloudpicturebackend.model.dto.file.PictureUploadResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * 图片上传模板
 */
@Slf4j
public abstract class PictureUploadTemplate {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    public PictureUploadResult uploadPicture(Object inputSource, String uploadPathPrefix) {
        //校验图片
        validPicture(inputSource);
        //图片上传地址
        String uuid = RandomUtil.randomString(16);
        String timeStamp = DateUtil.formatDate(new Date());
        String filename = getOriginalFilename(inputSource);
        String suffix = FileUtil.getSuffix(filename);
        String uploadPictureName = String.format("%s_%s.%s", timeStamp, uuid, suffix);
        String uploadPicturePath = String.format("/%s/%s", uploadPathPrefix, uploadPictureName);
        //解析结果并返回
        File file = null;
        try {
            //创建临时文件，获取文件到服务器
            file = File.createTempFile(uploadPicturePath, null);
            //处理文件来源
            processFile(inputSource, file);
            //上传到对象存储
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPicturePath, file);
            //获取图片信息对象
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            //获取到图片处理结果
            ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
            List<CIObject> objectList = processResults.getObjectList();
            //获取处理后的图片
            if (CollUtil.isNotEmpty(objectList)) {
                //获取webp图片
                CIObject compressedPicture = objectList.get(0);
                //缩略图默认为压缩图
                CIObject thumbNailPicture = compressedPicture;
                if (objectList.size() > 1) {
                    //获取缩略图
                    thumbNailPicture = objectList.get(1);
                }
                //封装压缩图的返回结果
                return buildResult(filename, compressedPicture, thumbNailPicture, imageInfo);
            }
            return buildResult(imageInfo, uploadPicturePath, filename, file);
        } catch (Exception e) {
            log.error("图片上传对象存储失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
        } finally {
            //临时文件清理
            deleteFile(file);
        }
    }

    /**
     * 封装返回结果
     *
     * @param filename          原始文件名
     * @param compressedPicture 压缩图片
     * @param thumbNailPicture
     * @param imageInfo 图片信息
     * @return
     */
    private PictureUploadResult buildResult(String filename, CIObject compressedPicture,
                                            CIObject thumbNailPicture, ImageInfo imageInfo) {
        //计算宽高
        int picHeight = compressedPicture.getHeight();
        int picWidth = compressedPicture.getWidth();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        //封装返回结果
        PictureUploadResult pictureUploadResult = new PictureUploadResult();
        //设置压缩后的原图地址
        pictureUploadResult.setUrl(cosClientConfig.getHost() + "/" + compressedPicture.getKey());
        pictureUploadResult.setPicName(FileUtil.mainName(filename));
        pictureUploadResult.setPicSize(Long.valueOf(compressedPicture.getSize()));
        pictureUploadResult.setPicWidth(picWidth);
        pictureUploadResult.setPicHeight(picHeight);
        pictureUploadResult.setPicScale(picScale);
        pictureUploadResult.setPicFormat(compressedPicture.getFormat());
        pictureUploadResult.setPicColor(imageInfo.getAve());
        //设置缩略图地址
        pictureUploadResult.setThumbnailUrl(cosClientConfig.getHost() + "/" + thumbNailPicture.getKey());

        //文件上传结果
        return pictureUploadResult;
    }


    /**
     * 处理输入图片或临时文件
     *
     * @param inputSource
     * @param file
     */
    protected abstract void processFile(Object inputSource, File file);

    /**
     * 获取图片名
     * @param inputSource
     * @return
     */
    protected abstract String getOriginalFilename(Object inputSource);

    /**
     * 校验传入文件(url / 拓扑)
     * @param inputSource
     */
    protected abstract void validPicture(Object inputSource);


    /**
     * 封装返回结果
     * @param imageInfo
     * @param uploadPicturePath
     * @param filename
     * @param file
     * @return
     */
    private PictureUploadResult buildResult(ImageInfo imageInfo, String uploadPicturePath,
                                            String filename, File file) {
        //计算宽高
        String picFormat = imageInfo.getFormat();
        int picHeight = imageInfo.getHeight();
        int picWidth = imageInfo.getWidth();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        //封装返回结果
        PictureUploadResult pictureUploadResult = new PictureUploadResult();
        pictureUploadResult.setUrl(cosClientConfig.getHost() + "/" + uploadPicturePath);
        pictureUploadResult.setPicName(FileUtil.mainName(filename));
        pictureUploadResult.setPicSize(FileUtil.size(file));
        pictureUploadResult.setPicWidth(picWidth);
        pictureUploadResult.setPicHeight(picHeight);
        pictureUploadResult.setPicScale(picScale);
        pictureUploadResult.setPicFormat(picFormat);
        pictureUploadResult.setPicColor(imageInfo.getAve());

        //文件上传结果
        return pictureUploadResult;
    }


    /**
     * 清理临时文件
     *
     * @param file file对象
     */
    public static void deleteFile(File file) {
        if (file != null) {
            //删除文件
            boolean delete = file.delete();
            if (!delete) {
                log.error("file delete error, filePath = {}", file.getAbsoluteFile());
            }
        }
    }

}
