package com.zx.spider.zxspider.main;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

/**
 * description:
 * <p>
 * author: ZhengXing
 * <p>
 * create: 2020-06-05 13:22
 **/
@Slf4j
public class TestMain {

    @SneakyThrows
    public static void main(String[] args) {
        System.setProperty("webdriver.chrome.driver", "D:\\ZhengXingWork\\chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        // 通过制定用户文件目录，获取到cookie
        options.addArguments("user-data-dir=C:\\Users\\Administrator\\AppData\\Local\\Google\\Chrome\\User Data");
//        options.addArguments("user-data-dir=D:\\ZhengXingWork\\User Data");
        // 删除某个警告标记
        options.addArguments("--start-maximized","allow-running-insecure-content","--test-type");

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, 10);
        try {
            // 直接跳转到章节目录页
            driver.get("https://book.qidian.com/info/1013212549#Catalog");
            // 获取 小说目录 窗口句柄
            String catalogWindowHandle = driver.getWindowHandle();
            // 尝试获取用户名
            String username = driver.findElement(By.id("nav-user-name")).getText();
            log.info("当前登录用户名:{}",username);
            if (!"一地霓虹".equals(username)) {
                try {
                    // 点击登录
                    driver.findElement(By.id("pin-login")).click();

                    //该登录会弹出一个 iframe，需要先切换到这个窗口，否则无法操作里面的元素
                    driver.switchTo().frame(driver.findElement(By.id("loginIfr")));

                    // 输入用户名密码
                    driver.findElement(By.id("username")).sendKeys("");
                    driver.findElement(By.id("password")).sendKeys("");
                    // 点击 登录
                    driver.findElement(By.className("login-button")).click();

                } catch (Exception e) {
                }finally {
                    // 切换回原窗口
                    driver.switchTo().window(catalogWindowHandle);
                    Thread.sleep(3000L);
                }
            }




            // 再获取一次 小说目录 窗口句柄
            catalogWindowHandle = driver.getWindowHandle();

            // 所有 卷列表
            List<WebElement> volumeList = driver.findElements(By.className("volume"));
            // 遍历所有卷
            for (WebElement volume : volumeList) {
                // 当前卷下所有章节
                List<WebElement> chapterList = volume.findElements(By.cssSelector("li > a"));
                for (WebElement chapter : chapterList) {
                    // 点击章节，会打开新窗口,但driver不会自动跳转到新窗口
                    chapter.click();
                    Thread.sleep(500L);
                    // 切换到第二个窗口
                    driver.switchTo().window(driver.getWindowHandles().toArray()[1].toString());
                    // 遍历当前章节下所有本章说按钮
                    List<WebElement> sectionList = driver.findElements(By.cssSelector(".review-count:not(.hidden)"));

                    // 依次点击每个段落本章说按钮
                    for (WebElement section : sectionList) {
                        section.click();
                        Thread.sleep(400L);
                        // 获取每个段落里的所有本章说
                        List<WebElement> reviewList = driver.findElements(By.className("review-item"));
                        // 遍历段落内所有本章说
                        for (WebElement review : reviewList) {
                            Thread.sleep(100L);
                            // 给每个本章说点赞
                            review.findElement(By.cssSelector(".review-1 >.review-footer > span:nth-child(2) > img")).click();;
                            // 打开本章说内 回复 窗口，给每个本章说的回复 点赞
                            WebElement replyButton = review.findElement(By.cssSelector(".review-1 >.review-footer > span:nth-child(1)"));
                            // 如果没有回复， 会显示"回复"； 有x条回复，会显示"x条回复"
                            if ("回复".equals(replyButton.getText())) {
                                break;
                            }
                            // 点击 x条回复 按钮
                            replyButton.click();
                            Thread.sleep(500L);
                            // 该条本章说下的回复列表
                            List<WebElement> replyList = driver.findElements(By.cssSelector("#j-repliesListWrap > .replies-ul > .review-wrap"));
                            for (WebElement reply : replyList) {
                                // 给每条回复点赞
                                reply.findElement(By.cssSelector(".review > .user-content > .review-footer > span:nth-child(3)")).click();
                            }
                            // 关闭 回复列表窗口
                            driver.findElement(By.cssSelector(".iconfont.close-btn")).click();
                        }
                    }

                    //关闭当前窗口
                    driver.close();
                    // 切换回原窗口
                    driver.switchTo().window(catalogWindowHandle);
                    log.info("end");
                }
            }





            log.info("end");
            //            WebElement firstResult = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h3>div")));
        } finally {
            // 退出，关闭所有窗口
            driver.quit();
        }
    }
}
