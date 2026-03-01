package com.taoxin.communitysharing.oss.business.strategy.impl;

import cn.hutool.core.lang.UUID;
import com.aliyun.oss.OSS;
import com.taoxin.communitysharing.oss.business.config.AliyunProperties;
import com.taoxin.communitysharing.oss.business.strategy.inface.FileStrategy;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;

@Slf4j
public class AliyunFileStrategy implements FileStrategy {
    @Resource
    private OSS ossClient;

    @Resource
    AliyunProperties aliyunProperties;
    @Override
    @SneakyThrows
    public String upload(MultipartFile file, String BucketName) {
        if (file.isEmpty() || file.getSize() <= 0) {
            log.error("[上传文件为空]");
            throw new RuntimeException("[上传文件为空]");
        }
        // 获取文件的原始名
        String originalFilename = file.getOriginalFilename();
        // 获取文件的文本类型
        String contentType = file.getContentType();
        // 获取文件的后缀名
        String suffixName = originalFilename.substring(originalFilename.lastIndexOf("."));
        // 生成存储对象的名称
        String key = UUID.randomUUID().toString().replace("-","");
        // 拼接存储对象的名称
        String objectName = String.format("%s%s", key, suffixName);
        log.info(">>>>>>> 阿里云文件上传 <<<<<<<\n" +
                "存储文件名：{}", objectName);
        ossClient.putObject(BucketName, objectName, new ByteArrayInputStream(file.getInputStream().readAllBytes()));
        String url = String.format("%s.%s.%s", BucketName, aliyunProperties.getEndpoint(), objectName);
        log.info("[阿里云] 上传文件成功，访问地址为：{}", url);
        return url;
    }
}
