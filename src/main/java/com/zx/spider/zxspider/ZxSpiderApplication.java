package com.zx.spider.zxspider;

import com.zx.spider.zxspider.config.QiDianAutoLikeSpiderProperties;
import com.zx.spider.zxspider.main.QiDianAutoLikeSpider;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ZxSpiderApplication implements ApplicationRunner{
    private final QiDianAutoLikeSpider qiDianAutoLikeSpider;
    private final QiDianAutoLikeSpiderProperties qiDianAutoLikeSpiderProperties;

    public ZxSpiderApplication(QiDianAutoLikeSpider qiDianAutoLikeSpider, QiDianAutoLikeSpiderProperties qiDianAutoLikeSpiderProperties) {
        this.qiDianAutoLikeSpider = qiDianAutoLikeSpider;
        this.qiDianAutoLikeSpiderProperties = qiDianAutoLikeSpiderProperties;
    }

    public static void main(String[] args) {
        SpringApplication.run(ZxSpiderApplication.class, args);

    }

    /**
     * 启动运行
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        qiDianAutoLikeSpider.run(qiDianAutoLikeSpiderProperties.getNovelId());
    }
}
