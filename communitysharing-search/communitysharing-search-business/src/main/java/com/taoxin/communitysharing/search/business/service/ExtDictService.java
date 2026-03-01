package com.taoxin.communitysharing.search.business.service;

import org.springframework.http.ResponseEntity;

public interface ExtDictService {
    /**
     * 获取热词更新
     * @return 热词更新
     */
    ResponseEntity<String> getHotUpdateExtDict();
}
