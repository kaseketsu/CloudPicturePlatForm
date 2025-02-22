package com.itflower.cloudpicturebackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.itflower.cloudpicturebackend.exception.BusinessException;
import com.itflower.cloudpicturebackend.exception.ErrorCode;
import com.itflower.cloudpicturebackend.exception.ThrowUtil;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

@Service
public class UrlPictureUpload extends PictureUploadTemplate {
    @Override
    protected void processFile(Object inputSource, File file) {
        String fileUrl = (String) inputSource;
        //下载文件
        HttpUtil.downloadFile(fileUrl, file);
    }

    @Override
    protected String getOriginalFilename(Object inputSource) {
        String fileUrl = (String) inputSource;
        return FileUtil.mainName(fileUrl);
    }

    @Override
    protected void validPicture(Object inputSource) {
        String fileUrl = (String) inputSource;
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
                !fileUrl.startsWith("http://") && !fileUrl.startsWith("https://"),
                ErrorCode.PARAMS_ERROR, "仅支持 HTTP 或 HTTPS 协议的文件地址, 您的文件地址为: " + fileUrl
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
//            String size = httpResponse.header("Content-Length");
//            if (StrUtil.isNotBlank(size)) {
//                try {
//                    long longSize = Long.parseLong(size);
//                    final long ONE_M = 1024 * 1024;
//                    ThrowUtil.throwIf(
//                            longSize > 2 * ONE_M, ErrorCode.PARAMS_ERROR, "文件大小不能超过2M"
//                    );
//                } catch (NumberFormatException e) {
//                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小格式异常");
//                }
//            }
        }
    }
}
