package com.itflower.cloudpicturebackend.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.itflower.cloudpicturebackend.common.ResultUtils;
import com.itflower.cloudpicturebackend.config.CosClientConfig;
import com.itflower.cloudpicturebackend.exception.BusinessException;
import com.itflower.cloudpicturebackend.exception.ErrorCode;
import com.itflower.cloudpicturebackend.exception.ThrowUtil;
import com.itflower.cloudpicturebackend.model.dto.file.PictureUploadResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 已废弃，改为upload包方法
 */
@Slf4j
@Service
@Deprecated
public class FileManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    public PictureUploadResult uploadPicture(MultipartFile multipartFile, String uploadPathPrefix) {
        //校验图片
        validPicture(multipartFile);
        //图片上传地址
        String uuid = RandomUtil.randomString(16);
        String timeStamp = DateUtil.formatDate(new Date());
        String filename = multipartFile.getOriginalFilename();
        String suffix = FileUtil.getSuffix(filename);
        String uploadPictureName = String.format("%s_%s.%s", timeStamp, uuid, suffix);
        String uploadPicturePath = String.format("/%s/%s", uploadPathPrefix, uploadPictureName);
        //解析结果并返回
        File file = null;
        try {
            //转移文件内容并上传
            file = File.createTempFile(uploadPicturePath, null);
            multipartFile.transferTo(file);
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPicturePath, file);
            //获取图片信息对象
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
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

            //文件上传结果
            return pictureUploadResult;
        } catch (Exception e) {
            log.error("图片上传对象存储失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
        } finally {
            //临时文件清理
            deleteFile(file);
        }
    }

    /**
     * 校验文件
     *
     * @param multipartFile 文件传输对象
     */
    private void validPicture(MultipartFile multipartFile) {
        //校验文件是否为空
        ThrowUtil.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
        //校验文件大小
        final int ONE_M = 1024 * 1024;
        long fileSize = multipartFile.getSize();
        ThrowUtil.throwIf(fileSize > 2 * ONE_M, ErrorCode.PARAMS_ERROR, "文件大小不能超过2MB");
        //校验文件后缀
        final List<String> ALLOW_PREFIX = Arrays.asList("jpeg", "png", "jpg", "webp");
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        ThrowUtil.throwIf(!ALLOW_PREFIX.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "文件类型错误");
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

    /**
     * 上传图片(url)
     * @param fileUrl 图片url
     * @param uploadPathPrefix 图片前缀
     * @return 图片上传结果
     */
    public PictureUploadResult uploadPictureByUrl(String fileUrl, String uploadPathPrefix) {
        //校验图片url
        validPicture(fileUrl);
        //图片上传地址
        String filename = FileUtil.mainName(fileUrl);
        //拼接地址
        String uuid = RandomUtil.randomString(16);
        String timeStamp = DateUtil.formatDate(new Date());
        String suffix = FileUtil.getSuffix(filename);
        String uploadPictureName = String.format("%s_%s.%s", timeStamp, uuid, suffix);
        String uploadPicturePath = String.format("/%s/%s", uploadPathPrefix, uploadPictureName);
        //解析结果并返回
        File file = null;
        try {
            //转移文件内容并上传
            file = File.createTempFile(uploadPicturePath, null);
            //下载文件
            HttpUtil.downloadFile(fileUrl, file);
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPicturePath, file);
            //获取图片信息对象
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
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

            //文件上传结果
            return pictureUploadResult;
        } catch (Exception e) {
            log.error("图片上传对象存储失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
        } finally {
            //临时文件清理
            deleteFile(file);
        }
    }

    /**
     * 根据url校验文件
     *
     * @param fileUrl 文教url
     */
    private void validPicture(String fileUrl) {
        //校验非空
        ThrowUtil.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "文件地址为空");
        //校验格式
        try {
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件地址格式不正确");
        }
        //校验协议
        ThrowUtil.throwIf(
                !fileUrl.startsWith("http://") || !fileUrl.startsWith("https://"),
                ErrorCode.PARAMS_ERROR, "仅支持 HTTP 或 HTTPS 协议的文件地址"
        );
        //发送head请求获取文件信息
        try (HttpResponse httpResponse = HttpUtil.createRequest(Method.HEAD, fileUrl)
                .execute()) {
            //未正常返回，无需执行下面的代码
            if (httpResponse.getStatus() != HttpStatus.HTTP_OK) {
                return;
            }
            //类型校验, 大小校验
            String type = httpResponse.header("Content-Type");
            //不为空则校验
            if (StrUtil.isNotBlank(type)) {
                final List<String> ALLOW_CONTENT_TYPES = Arrays.asList(
                        "image/jpeg", "image/jpg", "image/png", "image/webp"
                );
                ThrowUtil.throwIf(!ALLOW_CONTENT_TYPES.contains(type.toLowerCase()),
                        ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
            //文件大小
            String size = httpResponse.header("Content-Length");
            if (StrUtil.isNotBlank(size)) {
                try {
                    long longSize = Long.parseLong(size);
                    final long ONE_M = 1024 * 1024;
                    ThrowUtil.throwIf(
                            longSize > 2 * ONE_M, ErrorCode.PARAMS_ERROR, "文件大小不能超过2M"
                    );
                } catch (NumberFormatException e) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小格式异常");
                }
            }
        }
    }


}
