package com.itflower.cloudpicturebackend.api.imagesearch.sub;

import com.itflower.cloudpicturebackend.exception.BusinessException;
import com.itflower.cloudpicturebackend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 获取图片列表接口api
 */
@Slf4j
public class GetImageFirstUrlApi {

    public static String getImageFirstUrl(String searchResultUrl) {
        try{
            //获取html内容
            Document document = Jsoup.connect(searchResultUrl)
                    .timeout(5000)
                    .get();
            //获取script标签下所有内容
            Elements script = document.getElementsByTag("script");
            //遍历找到firstUrl内容，并进行正则表达式匹配
            for (Element e: script) {
                String content = script.html();
                if (content.contains("\"firstUrl\"")) {
                    Pattern pattern = Pattern.compile("\"firstUrl\"\\s*:\\s*\"(.*?)\"");
                    Matcher matcher = pattern.matcher(content);
                    if (matcher.find()) {
                        String firstUrl = matcher.group(1);
                        firstUrl = firstUrl.replace("\\/", "/");
                        return firstUrl;
                    }
                }
            }
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "检索失败");
        } catch (Exception e) {
            log.error("未能正确匹配firstUrl, ", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "检索失败");
        }
    }

    public static void main(String[] args) {
        String url = "https://graph.baidu.com/s?card_key=&entrance=GENERAL&extUiData[isLogoShow]=1&f=all&isLogoShow=1&session_id=13365309205516529562&sign=1266ae97cd54acd88139901737956132&tpl_from=pc";
        String imageFirstUrl = getImageFirstUrl(url);
        System.out.println(imageFirstUrl);

    }
}
