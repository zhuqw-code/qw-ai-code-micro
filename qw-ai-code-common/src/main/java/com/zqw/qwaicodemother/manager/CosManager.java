package com.zqw.qwaicodemother.manager;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.zqw.qwaicodemother.config.CosClientConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperties;
import org.springframework.stereotype.Component;

import java.io.File;


@Slf4j
@Component
@ConditionalOnBean(CosClientConfig.class)   // CosManager <- 有 CosClientConfig才加载  <-  yml文件配制cos才加载
public class CosManager {

    @Resource
    private COSClient COSClient;

    @Resource
    private CosClientConfig cosClientConfig;

    /**
     * 上传文件到COS
     * @param key 文件key，这里是文件名称
     * @param file 文件
     * @return 返回上传成功后的相应对象
     */
    public PutObjectResult putObject(String key, File file){
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucketName(), key, file);
            return COSClient.putObject(putObjectRequest);
        } catch (CosClientException e) {
            log.error("上传文件到COS失败，key: {}, file: {}", key, file, e);
            throw new RuntimeException("上传文件到COS失败");
        }
    }

    /**
     * 上传文件的方法，供外部调用
     * @param key 文件key，这里是文件名称
     * @param file 文件
     * @return 返回用户可以访问对象存储的地址
     */
    public String uploadToCos(String key, File file){
        // 上传，获取相应对象
        PutObjectResult putObjectResult = putObject(key, file);
        // 解析相应对象，返回给可以访问的地址
        String host = cosClientConfig.getHost();
        String accessKey = host + File.separator + key;
        return accessKey;
    }
}
