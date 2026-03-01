package com.taoxin.communitysharing.oss.business.strategy.inface;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件策略模式
 * @策略： 包含一个策略接口和一组实现该接口的策略类。
 * @说明： 调用者基于策略接口调用策略类中的方法，完成上传文件的功能。
 * 这是策略模式的基本结构。
 */
public interface FileStrategy {
    /***
     * 上传文件
     * @param file 文件
     * @param BucketName 存储桶名称
     * @return 文件访问地址
     * @MultipartFile 封装了上传文件的所有信息
     */
    String upload(MultipartFile file, String BucketName);
}
