package com.taoxin.communitysharing.oss.business.service.implement;

import com.alibaba.nacos.shaded.com.google.common.collect.Maps;
import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.oss.business.service.FileService;
import com.taoxin.communitysharing.oss.business.strategy.inface.FileStrategy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service // 服务层
@Slf4j
public class FileServiceImplement implements FileService {
    @Resource
    private FileStrategy fileStrategy;

    private static final String BucketName = "communitysharing";

    @Override
    public Response<?> uploadFile(MultipartFile file) {
        String url = fileStrategy.upload(file, BucketName);
        Map<String, String> map = Maps.newHashMap();
        map.put("url", url);
        return Response.success(map);
    }
}
