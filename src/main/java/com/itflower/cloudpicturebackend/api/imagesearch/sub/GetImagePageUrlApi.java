package com.itflower.cloudpicturebackend.api.imagesearch.sub;

import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.itflower.cloudpicturebackend.exception.BusinessException;
import com.itflower.cloudpicturebackend.exception.ErrorCode;
import com.itflower.cloudpicturebackend.exception.ThrowUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 获取以图搜图页面地址(step 1)
 */
@Slf4j
public class GetImagePageUrlApi {

    /**
     * 获取图片页面
     *
     * @param imageUrl
     * @return
     */
    public static String getImagePageUrl(String imageUrl) {
        //image
        //tn
        //from
        //image_source
        //sdkParams
        HashMap<String, Object> formData = new HashMap<>();
        formData.put("image", imageUrl);
        formData.put("tn", "pc");
        formData.put("from", "pc");
        formData.put("image_source", "PC_UPLOAD_URL");
        long uptime = System.currentTimeMillis();
        String url = "https://graph.baidu.com/upload?uptime=" + uptime;

        try {
            //发送请求
            HttpResponse httpResponse = HttpRequest.post(url)
                    .form(formData)
                    .timeout(5000)
                    .execute();
            ThrowUtil.throwIf(httpResponse.getStatus() != HttpStatus.HTTP_OK,
                    ErrorCode.OPERATION_ERROR, "接口调用失败");
            //解析响应
            String body = httpResponse.body();
            //将body转换为java的map
            Map<String, Object> res = JSONUtil.toBean(body, Map.class);
            //判空，判断状态
            ThrowUtil.throwIf(
                    res == null || !Integer.valueOf(0).equals(res.get("status")),
                    ErrorCode.OPERATION_ERROR, "接口调用失败"
            );
            //获取data, 转换为map类，判空
            Map<String, Object> data = (Map<String, Object>) res.get("data");
            ThrowUtil.throwIf(data == null, ErrorCode.OPERATION_ERROR, "返回数据为空");
            //对Url进行解码，判空
            String rawUrl = (String) data.get("url");
            String decodeUrl = URLUtil.decode(rawUrl, StandardCharsets.UTF_8);
            ThrowUtil.throwIf(decodeUrl == null, ErrorCode.OPERATION_ERROR, "未返回有效的url");
            return decodeUrl;
        } catch (Exception e) {
            log.error("调用百度以图搜图接口失败, ", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "搜索失败");
        }
    }

    public static void main(String[] args) {
        String imageUrl = "https://www.codefather.cn/logo.png";
        String res = getImagePageUrl(imageUrl);
        System.out.println(res);
    }
}
