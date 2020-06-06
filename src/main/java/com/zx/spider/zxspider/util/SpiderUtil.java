package com.zx.spider.zxspider.util;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * author:ZhengXing
 * datetime:2020-06-05 19:40
 */
public class SpiderUtil {

    /**
     * 根据正则截取字符
     */
    public static String subStringByRegexp(String text,String regexp) {
        if(StringUtils.isBlank(text) || StringUtils.isBlank(regexp))
            return "";
        Pattern pattern = Pattern.compile(regexp);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            // index 0 是text自己，1才是匹配到的字符串
            return  matcher.group(1);
        }
        return "";
    }

    /**
     * 控制滚动条向下拉到底
     *
     * 可能会触发ajax，继续加载出数据
     */
    public static void scroll2Bottom(WebDriver driver) {
        ((JavascriptExecutor) driver).executeScript("document.documentElement.scrollTop=50000");
    }


    /**
     * 控制滚动条向下拉到底
     *
     * 可能会触发ajax，继续加载出数据
     */
    public static void scroll2Bottom(WebDriver driver,String getElement) {
        ((JavascriptExecutor) driver).executeScript("document."+getElement+".scrollTop=50000");
    }



    public static void main(String[] args) {
        System.out.println(subStringByRegexp("共100条帖子", "共(\\d+)条帖子"));
    }
}
