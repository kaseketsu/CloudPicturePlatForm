package com.itflower.cloudpicturebackend.api.imagesearch.sub;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.itflower.cloudpicturebackend.api.imagesearch.model.ImageResult;
import com.itflower.cloudpicturebackend.exception.BusinessException;
import com.itflower.cloudpicturebackend.exception.ErrorCode;
import com.itflower.cloudpicturebackend.exception.ThrowUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class GetImageListApi {
    /**
     * 获取图片列表
     * @param url
     * @return
     */
    public static List<ImageResult> getImageList(String url) {
        try {
            //创建get请求
            HttpResponse response = HttpUtil.createGet(url).execute();
            //获取相应信息
            int status = response.getStatus();
            ThrowUtil.throwIf(status != 200, ErrorCode.OPERATION_ERROR, "获取数据失败");
            String body = response.body();
            return processResponse(body);
        } catch (Exception e) {
            log.error("接口调用失败, ", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
        }
    }

    public static List<ImageResult> processResponse(String responseBody) {
        JSONObject jsonObject = new JSONObject(responseBody);
        ThrowUtil.throwIf(!jsonObject.containsKey("data"), ErrorCode.OPERATION_ERROR, "未获取到图片列表");
        JSONObject data = jsonObject.getJSONObject("data");
        ThrowUtil.throwIf(!data.containsKey("list"), ErrorCode.OPERATION_ERROR, "未获取到图片列表");
        JSONArray jsonArray = data.getJSONArray("list");
        return JSONUtil.toList(jsonArray, ImageResult.class);
    }

    public static void main(String[] args) {
        String url = "https://graph.baidu.com/ajax/pcsimi?carousel=503&entrance=GENERAL&extUiData%5BisLogoShow%5D=1&inspire=general_pc&limit=30&next=2&render_type=card&session_id=13365309205516529562&sign=1266ae97cd54acd88139901737956132&tk=a44ea&tpl_from=pc";
        List<ImageResult> imageList = getImageList(url);
        System.out.println("搜索成功: " + imageList);
    }
}
