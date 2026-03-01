package com.taoxin.communitysharing.oss.business.strategy.impl;

import cn.hutool.core.lang.UUID;
import com.taoxin.communitysharing.oss.business.config.MinioProperties;
import com.taoxin.communitysharing.oss.business.strategy.inface.FileStrategy;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

/**
 * Minio文件存储策略实现类
 */
@Slf4j
public class MinioFileStrategy implements FileStrategy {
    @Resource
    private MinioClient minioClient;
    @Resource
    private MinioProperties minioProperties;
    @Override
    @SneakyThrows // 忽略方法中的异常
    public String upload(MultipartFile file, String BucketName) {
        // 1. 判断文件是否为空
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
        log.info(">>>>>>> Minio文件上传 <<<<<<<\n" +
                "存储文件名：{}", objectName);
        // 上传文件
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(BucketName)
                .object(objectName)
                .stream(file.getInputStream(), file.getSize(), -1) // file.getInputStream() 产生IO异常
                .contentType(contentType)
                .build());
        // 获取文件访问路径
        String url = String.format("%s/%s/%s", minioProperties.getEndpoint(), BucketName, objectName);
        log.info("[Minio] 上传文件成功，访问地址为：{}", url);
        return url; // 返回文件访问路径
    }
}
