package com.taoxin.communitysharing.oss.business.controller;

import com.taoxin.communitysharing.framework.business.context.holder.LoginUserContextHolder;
import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.oss.business.service.FileService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {
    @Resource
    FileService fileService;
    @PostMapping(value = "/upload",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response<?> upload(@Validated @RequestPart("file") MultipartFile file) {
        Long userId = LoginUserContextHolder.getUserId();
        log.info(">>>>>>> 文件上传 <<<<<<<");
        log.info("用户ID: {}", userId);
        return fileService.uploadFile(file);
    }
}
