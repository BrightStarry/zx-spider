#### 爬虫

* 起点本章说自动点赞爬虫
    * 设置小说目录页url,自动登录（如果弹出验证码暂时需要手动）,打开每个章节，对所有本章说及本章说下回复点赞
    * TODO: 处理本章说数量超过100条的段落（100条内滚动条分页，超过100条分页按钮分页）
    * [自定义配置参考](src/main/java/com/zx/spider/zxspider/config/QiDianAutoLikeSpiderProperties.java)
    * 在yml中配置好参数，启动SpringBoot即可

#### 相关文档
* [selenium文档](https://www.selenium.dev/documentation/zh-cn/)
* [selenium Github wiki](https://github.com/SeleniumHQ/selenium/wiki/ChromeDriver)
* [chromeDriver下载地址1](http://chromedriver.storage.googleapis.com/index.html)
* [chromeDriver下载地址2](https://sites.google.com/a/chromium.org/chromedriver/)

#### selenium等待方式
* 直接Thread.sleep()强制等待若干时间
* 隐式等待

~~~
当selenium 找不到某个元素而要抛出异常前会重新在dom对象中查找这个元素，直到达到隐式等待里指定的时间，也就是指定一个最长等待时间。
这个等待方式，对 webdriver 对象整个生命周期都生效。也就是只要指定一次，后续各种元素查找操作都会有这个最长等待时间。

driver.manage().timeouts().implicitlyWait(3000, TimeUnit.MILLISECONDS);

~~~


* 显示等待

~~~
显示等待是等待指定元素设置的等待时间，在设置时间内，默认每隔0.5s检测一次当前的页面这个元素是否存在，如果在规定的时间内找到了元素则执行相关操作，如果超过设置时间检测不到则抛出异常。默认抛出异常为：NoSuchElementException

driver.get("http://www.baidu.com");
//声明一个Action对象
Actions action=new Actions(driver);
//鼠标移动到  更多产品 上
action.moveToElement(driver.findElement(By.xpath("//a[text()='更多产品']"))).perform();
//显示等待时间10s 等   全部产品>>  出现
WebDriverWait w=new WebDriverWait(driver,10);
w.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//a[text()='全部产品>>']")));
//等待的元素出现后点击  音乐  
WebElement cp=driver.findElement(By.xpath("//a[text()='音乐']"));
cp.click();
//断言音乐页面的Title值为   千千音乐-听见世界
Assert.assertEquals("千千音乐-听见世界",driver.getTitle());
~~~


#### bug
* chromedriver版本必须和本机安装的chrome版本号一致（至少大版本号需要一致）
* 额外导入okHttpClient的话需要注意版本问题
* StaleElementReferenceException: stale element reference: element is not attached to the page document

~~~
所引用的元素已过时，不再依附于当前页面。通常情况下，这是因为页面进行了刷新或跳转，解决方法是，重新使用 findElement 或 findElements 方法进行元素定位即可。

我遇到的情况是页面异步加载的数据还未完全加载，就会出现这种情况，解决方法是
判断异步数据列表的最后一条数据是否已经加载成功；
~~~