package com.zqw.qwaicodemother.service;

public interface ScreenshotService {

    /**
     * 提供通用的更具网页截图 + 存储到COS对象存储中
     * @param webUrl 待截图网页
     * @return 返回COS对象存储可访问页面
     */
    String generateAndUploadScreenshot(String webUrl);
}
