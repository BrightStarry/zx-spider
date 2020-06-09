package com.zx.spider.zxspider.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

/**
 * author:ZhengXing
 * datetime:2020-06-06 8:33
 * 起点自动点赞爬虫属性配置
 */
@Component
@Slf4j
@ConfigurationProperties(prefix = "qidian.auto-like")
@Data
@Validated
public class QiDianAutoLikeSpiderProperties {

    /**
     * 起点小说id
     */
    @NotBlank(message = "起点小说id不能为空")
    private String novelId;

    /**
     * chromeDriver.exe程序路径
     */
    @NotBlank(message = "chromeDriver.exe路径不允许为空")
    private String chromeDriverExePath;

    /**
     * chrome 用户文件目录
     * 如果和本地chrome程序默认目录相同，则可共享cookie
     * 默认路径一般为
     * %userPath%\AppData\Local\Google\Chrome\User Data
     * 例如 C:\Users\97038\AppData\Local\Google\Chrome\User Data
     *
     * 为null时不设置
     */
    private String chromeUserDataPath;

    /**
     * 起点用户名
     */
    @NotBlank(message = "起点账号密码不允许为空")
    private String username;

    /**
     * 起点密码
     */
    @NotBlank(message = "起点账号密码不允许为空")
    private String password;


    /**
     * 全局 隐式等待时间,毫秒
     * 为null时不设置隐式等待
     */
    private Long implicitlyWaitTime;

    /**
     * 全局 脚本超时时间,毫秒
     * 为null时不设置隐式等待
     */
    private Long scriptTimeout;

}
