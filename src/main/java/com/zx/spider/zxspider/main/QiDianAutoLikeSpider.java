package com.zx.spider.zxspider.main;

import com.zx.spider.zxspider.config.QiDianAutoLikeSpiderProperties;
import com.zx.spider.zxspider.util.CustomExpectedConditions;
import com.zx.spider.zxspider.util.SpiderUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * author:ZhengXing
 * datetime:2020-06-06 8:32
 * 起点自动点赞爬虫
 */
@Component
@Slf4j
public class QiDianAutoLikeSpider  {

    private final QiDianAutoLikeSpiderProperties qiDianAutoLikeSpiderProperties;

    // 浏览器驱动
    private final WebDriver driver;
    // 上一个 窗口句柄
    private String lastWindowHandle;
    // 默认的显示等待策略
    private WebDriverWait wait;

    public QiDianAutoLikeSpider(
            QiDianAutoLikeSpiderProperties qiDianAutoLikeSpiderProperties) {
        this.qiDianAutoLikeSpiderProperties = qiDianAutoLikeSpiderProperties;
        this.driver = buildDriver();
    }



    /**
     * 销毁
     */
    private void destroy() {
        // 退出，关闭所有窗口
        driver.quit();
    }


    /**
     * 构建 浏览器驱动
     */
    private WebDriver buildDriver() {
        // 设置chromeDriver.exe程序路径
        System.setProperty("webdriver.chrome.driver", qiDianAutoLikeSpiderProperties.getChromeDriverExePath());
        ChromeOptions options = new ChromeOptions();
        // 设置 用户数据 目录
        if(StringUtils.isNotBlank(qiDianAutoLikeSpiderProperties.getChromeUserDataPath()))
            options.addArguments("user-data-dir="+qiDianAutoLikeSpiderProperties.getChromeUserDataPath());
        // 删除某个警告
        options.addArguments("--start-maximized","allow-running-insecure-content","--test-type");
        /**
         * 页面加载策略，默认为{@link PageLoadStrategy.NORMAL},即完全加载页面
         * 可配置为
         * {@link PageLoadStrategy.EAGER},加载并解析html，但不加载样式、图片和子frame
         * {@link PageLoadStrategy.NONE},只加载html
         */
        options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
        WebDriver driver = new ChromeDriver(options);
        // 设置隐式等待时间
        if(qiDianAutoLikeSpiderProperties.getImplicitlyWaitTime() != null)
            driver.manage().timeouts().implicitlyWait(qiDianAutoLikeSpiderProperties.getImplicitlyWaitTime(), TimeUnit.MILLISECONDS);
        // 设置脚本超时时间
        driver.manage().timeouts().setScriptTimeout(qiDianAutoLikeSpiderProperties.getScriptTimeout(), TimeUnit.MILLISECONDS);
        // 设置默认的显示等待策略
        this.wait = new WebDriverWait(driver, 3, 100);
        return driver;
    }

    /**
     * 启动
     * @param novelId 小说id
     *                例如url:https://book.qidian.com/info/1019600799 小说id为1019600799
     */
    public void run(String novelId) {
        try {
            driver.get("https://book.qidian.com/info/"+novelId+"#Catalog"); // 打开小说目录页
            setLastWindowHandleAsCurrent();//保存当前窗口句柄
            login();// 登录
            catalogHandle();// 处理目录
        } catch (Exception e) {
            log.error("",e);
        } finally {
            destroy();
        }
    }

    /**
     * 对目录进行处理
     */
    private void catalogHandle() {
        // 所有 卷列表
        List<WebElement> volumeList = driver.findElements(By.className("volume"));
        // 遍历所有卷
        volumeList.forEach(
                // 处理给个卷
                volumeHandle(
                        // 处理每一章
                        chapterHandle(
                                // 处理每一段落
                                sectionHandle(
                                        // 处理每条本章说
                                        reviewHandle(
                                                // 处理每条回复
                                                replyHandle()
                                        )
                                )
                        )
                )
        );
    }

    /**
     * 对每个卷进行处理
     */
    private Consumer<? super WebElement> volumeHandle(Consumer<? super WebElement> chapterHandle) {
        return volume->{
            // 当前卷下所有章节
            List<WebElement> chapterList = volume.findElements(By.cssSelector("li > a"));
            // 对每个章节进行处理
            chapterList.forEach(chapterHandle);
        };
    }

    /**
     * 对每个章节进行处理
     */
    private Consumer<? super WebElement> chapterHandle(Consumer<? super WebElement> sectionHandle) {
        return chapter->{
            // 点击章节，会打开新窗口,但driver不会自动跳转到新窗口
            chapter.click();
            switchToIndexWindow(1);
            // 获取章节下所有段落本章说按钮
            List<WebElement> sectionList = driver.findElements(By.cssSelector(".review-count:not(.hidden)"));
            // 对每个段落进行处理
            sectionList.forEach(sectionHandle);
            // 处理完成后关闭当前窗口
            driver.close();
            // 切换回原窗口
            switchToLastWindow();
        };
    }

    /**
     * 对每个段落进行处理
     */
    private Consumer<? super WebElement> sectionHandle(Consumer<? super WebElement> reviewHandle) {
        return section->{
            // 点击改段落的本章说按钮打开本章说窗口
            section.click();
            // 帖子总数
            WebElement totalElement = driver.findElement(By.cssSelector(".total > p"));
            try {
                // 显示等待
                wait.until(CustomExpectedConditions.textMatchingRegexpPresentInElement(totalElement, Pattern.compile("共(\\d+)条帖子")));
            } catch (Exception e) {
                // TODO
            }

            // 获取本章说总数
            String numString = SpiderUtil.subStringByRegexp(totalElement.getText(),"共(\\d+)条帖子");
            int num = Integer.parseInt(numString);
            if (num > 20) {
                for (int i = 0; i < num / 20; i++) {
                    // 下拉滚动条
                    SpiderUtil.scroll2Bottom(driver,"getElementById(\"j-reviewWrapList\")");
                    // 等待滚动条数据加载, 确保本章说数量大于当前滚动条加载后的数量后再继续
//                    sleep(300);
                    int finalI = i;
                    wait.until(CustomExpectedConditions.elementsMatchingPresentInElement(
                            By.cssSelector("#j-reviewWrapList > ul > .review-item"),
                            items -> items.size() >= ((finalI * 20)+20)));
                }
            }
            // 获取每个段落里的所有本章说
            List<WebElement> reviewList = driver.findElements(By.cssSelector("#j-reviewWrapList > ul > .review-item"));
            // 对所有本章说进行处理
            reviewList.forEach(reviewHandle);
        };
    }

    /**
     * 对每个本章说进行处理
     */
    private Consumer<? super WebElement> reviewHandle(Consumer<? super WebElement> replyHandle) {
        return review->{
            // 点赞按钮
            WebElement likeButton = review.findElement(By.cssSelector(".review-1 >.review-footer > span:nth-child(2) > img"));
            // 如果未点赞，才点赞
            if (!likeButton.getAttribute("src").contains("d703b36c0eb8")) {
                likeButton.click();
            }
            // 处理本章说下的回复
            replyHandle().accept(review);
        };
    }

    /**
     * 对每个本章说下的回复进行处理
     */
    private Consumer<? super WebElement> replyHandle() {
        return review ->{
            // 本章说下的回复按钮
            WebElement replyButton = review.findElement(By.cssSelector(".review-1 >.review-footer > span:nth-child(1)"));
            // 如果没有回复，会显示"回复"； 有x条回复，会显示"x条回复"
            if ("回复".equals(replyButton.getText())) {
                return;
            }
            // 点击 x条回复 按钮
            replyButton.click();
            // 回复数
            WebElement totalElement = driver.findElement(By.cssSelector("#review-replies-modal > .review-replies-popup > .review-wrap > .review > .user-content > .review-footer > span:nth-child(2)"));
            try {
                // 显示等待
                wait.until(CustomExpectedConditions.textMatchingRegexpPresentInElement(totalElement, Pattern.compile("(\\d+)条回复")));
            } catch (Exception e) {
                // TODO
            }
            // 获取该条本章说下的回复数
            String numString = SpiderUtil.subStringByRegexp(totalElement.getText(), "(\\d+)条回复");
            int num = Integer.parseInt(numString);
            if (num > 20) {
                for (int i = 0; i < num / 20; i++) {
                    SpiderUtil.scroll2Bottom(driver,"getElementById(\"j-repliesListWrap\")");
                    // 等待回复加载
//                    sleep(300);
                    int finalI = i;
                    wait.until(CustomExpectedConditions.elementsMatchingPresentInElement(
                            By.cssSelector("#j-repliesListWrap > .replies-ul > .review-wrap"),
                            items -> items.size() >= ((finalI * 20) + 20)));
                }
            }

            try {
                // 显示等待,确保回复列表及列表中最后一个楼层的内容已加载
                wait.until(CustomExpectedConditions.textNotBlankPresentInElement(driver.findElement(By.cssSelector("#j-repliesListWrap > .replies-ul > .review-wrap:nth-last-child(1) > .review > .user-content > .review-footer > span:nth-child(1)"))));
            } catch (Exception e) {
                // TODO
            }

            // 该条本章说下的回复列表
            List<WebElement> replyList = driver.findElements(By.cssSelector("#j-repliesListWrap > .replies-ul > .review-wrap"));
            for (WebElement reply : replyList) {
                // 给每条回复点赞
                WebElement replyLikeButton = reply.findElement(By.cssSelector(".review > .user-content > .review-footer > span:nth-child(3) > img"));
                // 如果未点赞，才点赞
                if (!replyLikeButton.getAttribute("src").contains("d703b36c0eb8")) {
                    replyLikeButton.click();
                }
            }
            // 关闭 回复列表窗口
            driver.findElement(By.cssSelector(".iconfont.close-btn")).click();
        };
    }

    /**
     * 登录
     */
    private void login() {
        By usernameBy = By.id("nav-user-name");
        try {
            // 显示等待3秒, 重试间隔为200毫秒
            wait.until(CustomExpectedConditions.textNotBlankPresentInElement(driver.findElement(usernameBy)));
        } catch (Exception ignored) {
            // 抛出异常，表示当前未登录
            try {
                // 点击登录
                driver.findElement(By.id("pin-login")).click();
                //该登录会弹出一个 iframe，需要先切换到这个窗口，否则无法操作里面的元素
                driver.switchTo().frame(driver.findElement(By.id("loginIfr")));

                // 切换到账号密码登录
                driver.findElement(By.cssSelector("#j_loginTab > ul > li:nth-child(1)")).click();
                // 输入用户名密码
                driver.findElement(By.id("username")).sendKeys(qiDianAutoLikeSpiderProperties.getUsername().trim());
                driver.findElement(By.id("password")).sendKeys(qiDianAutoLikeSpiderProperties.getPassword().trim());
                // 点击 登录
                driver.findElement(By.className("login-button")).click();
                // 如果弹出了验证码
                WebElement bodyWrap = driver.findElement(By.id("bodyWrap"));
                // 等待，此时需要手动验证
                if (bodyWrap != null) {
                    sleep(5000);
                }
            } catch (Exception ignored2) {
            }finally {
                switchToLastWindow();// 切换回原窗口
            }
        }
        // 尝试获取用户名
        String username = driver.findElement(usernameBy).getText();
        if (StringUtils.isBlank(username)) {
            throw new RuntimeException("登录失败，无法获取到用户名...");
        }
        log.info("当前登录用户名:{}",username);
    }

    /**
     * 将 {@link #lastWindowHandle} 设置为当前窗口
     */
    private void setLastWindowHandleAsCurrent() {
        this.lastWindowHandle = driver.getWindowHandle();
    }

    /**
     * 切换回上一个窗口
     */
    private void switchToLastWindow() {
        driver.switchTo().window(lastWindowHandle);
    }

    /**
     * 切换到第x个窗口
     * index 从0开始
     */
    private void switchToIndexWindow(int index) {
        driver.switchTo().window(driver.getWindowHandles().toArray()[index].toString());
    }


    /**
     * 线程等待
     */
    @SneakyThrows
    private void sleep(long millis) {
        Thread.sleep(millis);
    }
}
