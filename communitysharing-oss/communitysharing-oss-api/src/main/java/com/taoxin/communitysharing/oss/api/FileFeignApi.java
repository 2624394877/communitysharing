package com.taoxin.communitysharing.oss.api;

import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.oss.config.FeignFormConfig;
import com.taoxin.communitysharing.oss.constant.ApiConstant;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = ApiConstant.SERVICE_NAME, configuration = FeignFormConfig.class) // 定义为feign客户端, 传值：服务名
public interface FileFeignApi {
    String PREFIX = "/file"; // 前缀服务请求的路径

    /**
     * 对应的服务方法，与控制器的方法一致，即表示调用对应的控制器方法，value为访问请求的完整路径
     * @return
     */
    @PostMapping(value = PREFIX + "/upload",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Response<?> upload(@RequestPart(value = "file") MultipartFile file);
}
