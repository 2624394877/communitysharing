package com.taoxin.communitysharing.user.business.rpc;

import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.oss.api.FileFeignApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import static org.apache.commons.lang3.StringUtils.indexOf;

@Service
public class OssFeignService {
    @Resource
    private FileFeignApi fileFeignApi;

    public String upload(MultipartFile file) {
        Response<?> response = fileFeignApi.upload(file);
        if (!response.isSuccess()) {
            return null;
        }
        String url = response.getData().toString();
        return url.substring(indexOf(url, "=")+1, url.length()-1); // 获取上传成功后的文件路径
    }
}
