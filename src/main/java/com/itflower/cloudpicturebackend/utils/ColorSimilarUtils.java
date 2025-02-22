package com.itflower.cloudpicturebackend.utils;

import java.awt.*;

public class ColorSimilarUtils {

    private ColorSimilarUtils() {
    }

    /**
     * 计算两个颜色相似度
     *
     * @param color1
     * @param color2
     * @return
     */
    public static double calculateSimilarity(Color color1, Color color2) {
        int red1 = color1.getRed();
        int green1 = color1.getGreen();
        int blue1 = color1.getBlue();

        int red2 = color2.getRed();
        int green2 = color2.getGreen();
        int blue2 = color2.getBlue();

        double dis = Math.sqrt(Math.pow(red1 - red2, 2) + Math.pow(green1 - green2, 2) + Math.pow(blue1 - blue2, 2));

        return 1 - dis / Math.sqrt(3 * Math.pow(255, 2));
    }


    public static double calculateSimilarity(String hexColor1, String hexColor2) {
        Color color1 = Color.decode(hexColor1);
        Color color2 = Color.decode(hexColor2);
        return calculateSimilarity(color1, color2);
    }

    public static void main(String[] args) {
        Color color1 = Color.decode("0xFF000");
        Color color2 = Color.decode("0xFE0101");
        double similarity = calculateSimilarity(color1, color2);
        System.out.println("颜色相似度为: " + similarity);

        String hexColor1 = "0xFF000";
        String hexColor2 = "0xFE0101";
        double hexSimilarity = calculateSimilarity(hexColor1, hexColor2);
        System.out.println("十六进制颜色相似度为: " + similarity);
    }
}
