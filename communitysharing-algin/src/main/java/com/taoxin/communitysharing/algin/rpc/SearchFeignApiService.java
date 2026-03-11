package com.taoxin.communitysharing.algin.rpc;

import com.taoxin.communitysharing.search.api.SearchFeignApi;
import com.taoxin.communitysharing.search.dto.request.RebuildContentDocReqDTO;
import com.taoxin.communitysharing.search.dto.request.RebuildUserDocReqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SearchFeignApiService {
    @Resource
    private SearchFeignApi searchFeignApi;

    public void rebuildContentDoc(String contentId) {
        RebuildContentDocReqDTO reqDTO = RebuildContentDocReqDTO.builder()
                .contentId(contentId)
                .build();
        searchFeignApi.rebuildContentDoc(reqDTO);
    }

    public void rebuildUserDoc(String userId) {
        RebuildUserDocReqDTO reqDTO = RebuildUserDocReqDTO.builder()
                .userId(userId)
                .build();
        searchFeignApi.rebuildUserDoc(reqDTO);
    }
}
