package com.zx.spider.zxspider.util;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * author:ZhengXing
 * datetime:2020-06-06 11:48
 * 自定义的 selenium 等待条件
 *
 * 参考{@link org.openqa.selenium.support.ui.ExpectedConditions}，但该类将构造函数设为private无法继承
 *
 * ps:
 * 我认为该类下的方法最好传入 WebElement类型参数，而不是By类型，因为By类型每次调用都需要在WebDriver中查找
 */
public class CustomExpectedConditions {

    /**
     * 元素集合,需要符合一定条件
     */
    public static ExpectedCondition<Boolean> elementsMatchingPresentInElement(final By by,final Predicate<List<WebElement>> predicate) {
        return new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                try {
                    List<WebElement> elements = driver.findElements(by);
                    return predicate.test(elements);
                } catch (StaleElementReferenceException e) {
                    return null;
                }
            }
            @Override
            public String toString() {
                return String.format("text matching regexp present in element %s", by);
            }
        };
    }




    /**
     * 当前元素的text 能匹配对应正则
     */
    public static ExpectedCondition<Boolean> textMatchingRegexpPresentInElement(final WebElement element,final Pattern pattern) {
        return new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                try {
                    String elementText = element.getText();
                    return pattern.matcher(elementText).find();
                } catch (StaleElementReferenceException e) {
                    return null;
                }
            }
            @Override
            public String toString() {
                return String.format("text matching regexp present in element %s", element);
            }
        };
    }

    /**
     * 当前元素的text 符合一定条件
     */
    public static ExpectedCondition<Boolean> textMatchingPresentInElement(final WebElement element,final Predicate<String> predicate) {
        return new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                try {
                    String elementText = element.getText();
                    return predicate.test(elementText);
                } catch (StaleElementReferenceException e) {
                    return null;
                }
            }
            @Override
            public String toString() {
                return String.format("text  not blank present in element %s", element);
            }
        };
    }

    /**
     * 当前元素的text 不是空的
     */
    public static ExpectedCondition<Boolean> textNotBlankPresentInElement(final WebElement element) {
        return new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                try {
                    String elementText = element.getText();
                    return StringUtils.isNotBlank(elementText);
                } catch (StaleElementReferenceException e) {
                    return null;
                }
            }
            @Override
            public String toString() {
                return String.format("text  not blank present in element %s", element);
            }
        };
    }
}
