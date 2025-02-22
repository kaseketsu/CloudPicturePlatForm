package com.itflower.cloudpicturebackend.api.imagesearch;

import com.itflower.cloudpicturebackend.api.imagesearch.model.ImageResult;
import com.itflower.cloudpicturebackend.api.imagesearch.sub.GetImageFirstUrlApi;
import com.itflower.cloudpicturebackend.api.imagesearch.sub.GetImageListApi;
import com.itflower.cloudpicturebackend.api.imagesearch.sub.GetImagePageUrlApi;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ImageSearchApiFacade {

    /**
     * 接口组合调用，获取图片列表
     * @param imageUrl
     * @return
     */
    public static List<ImageResult> searchImage(String imageUrl) {
        String imagePageUrl = GetImagePageUrlApi.getImagePageUrl(imageUrl);
        String imageFirstUrl = GetImageFirstUrlApi.getImageFirstUrl(imagePageUrl);
        List<ImageResult> imageList = GetImageListApi.getImageList(imageFirstUrl);
        return imageList;
    }

    public static void main(String[] args) {
        List<ImageResult> imageResults = searchImage("https://www.codefather.cn/logo.png");
        System.out.println("结果列表: " + imageResults);
    }
}
