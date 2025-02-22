package com.itflower.cloudpicturebackend.controller;

import cn.hutool.http.server.HttpServerResponse;
import com.itflower.cloudpicturebackend.annotation.AuthCheck;
import com.itflower.cloudpicturebackend.common.BaseResponse;
import com.itflower.cloudpicturebackend.common.ResultUtils;
import com.itflower.cloudpicturebackend.constant.UserConstant;
import com.itflower.cloudpicturebackend.exception.BusinessException;
import com.itflower.cloudpicturebackend.exception.ErrorCode;
import com.itflower.cloudpicturebackend.manager.CosManager;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;


@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private CosManager cosManager;

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/test/upload")
    public BaseResponse<String> testUpload(@RequestParam("file") MultipartFile multipartFile) {
        String filename = multipartFile.getOriginalFilename();
        String filePath = String.format("/test/%s", filename);
        File file = null;
        try {
            //转移文件内容并上传
            file = File.createTempFile(filePath, null);
            multipartFile.transferTo(file);
            cosManager.putObject(filePath, file);
            //返回文件路径
            return ResultUtils.success(filePath);
        } catch (Exception e) {
            log.error("file upload error, filePath = {}", filePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
        } finally {
            if (file != null) {
                //删除文件
                boolean delete = file.delete();
                if (!delete) {
                    log.error("file delete error, filePath = {}", filePath);
                }
            }
        }
    }


    /**
     * 测试下载
     * @param filePath 文件路径
     * @param response servlet响应
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/test/download")
    public void testDownloadFile(String filePath, HttpServletResponse response) throws IOException {
        COSObjectInputStream cosObjectInput = null;
        try {
            COSObject object = cosManager.getObject(filePath);
            cosObjectInput = object.getObjectContent();
            byte[] byteArray = IOUtils.toByteArray(cosObjectInput);
            //设置响应头
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + filePath);
            //写入相应
            response.getOutputStream().write(byteArray);
            response.getOutputStream().flush();
        } catch(Exception e) {
            log.error("file download failed, filePath = {}", filePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载失败");
        } finally {
            //释放流
            if (cosObjectInput != null) {
                cosObjectInput.close();
            }
        }
    }
}
