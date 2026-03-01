package com.taoxin.communitysharing.search.business.service.implement;

import com.taoxin.communitysharing.search.business.service.ExtDictService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ExtDictServiceImplement implements ExtDictService {
    @Value("${elasticsearch.hotUpdateExtDict}")
    private String hotUpdateExtDict;

    /**
     * 获取热更新字典
     * @说明(官方文档)：
     * 1. HTTP 请求需要返回两个 header，一个是 Last-Modified，另一个是ETag。这两个都是字符串类型，如果其中一个发生更改，ik 插件就会获取新的分词来更新词库。
     * 2. HTTP 请求返回的内容格式为每行一个单词，换行符用表示\n。
     */
    @Override
    public ResponseEntity<String> getHotUpdateExtDict() {
        try {
            // 获取文件最后修改时间
            Path path = Paths.get(hotUpdateExtDict);
            long lastTime = Files.getLastModifiedTime(path).toMillis();
            // 获取文件内容
            String fileContent = Files.lines(path).collect(Collectors.joining("\n"));
            String etag = String.valueOf(fileContent.hashCode());
            // 创建响应头
            HttpHeaders headers = new HttpHeaders();
            headers.add("ETag", etag);
            headers.setContentType(MediaType.valueOf("text/plain;charset=UTF-8"));
            // 创建响应体
            return ResponseEntity.ok()
                    .headers(headers)
                    .lastModified(lastTime)
                    .body(fileContent);
        } catch (Exception e) {
            log.error("获取热更新字典失败", e);
        }
        return null;
    }
}
