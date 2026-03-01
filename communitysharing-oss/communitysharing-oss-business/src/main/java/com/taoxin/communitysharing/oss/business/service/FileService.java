package com.taoxin.communitysharing.oss.business.service;

import com.taoxin.communitysharing.common.response.Response;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    Response<?> uploadFile(MultipartFile file);
}
