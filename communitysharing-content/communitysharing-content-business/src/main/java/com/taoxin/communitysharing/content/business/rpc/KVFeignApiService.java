package com.taoxin.communitysharing.content.business.rpc;

import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.KV.api.KVFeignApi;
import com.taoxin.communitysharing.KV.dto.request.AddSharingContentRequestDTO;
import com.taoxin.communitysharing.KV.dto.request.DeleteSharingContentRequestDTO;
import com.taoxin.communitysharing.KV.dto.request.FindSharingContentRequestDTO;
import com.taoxin.communitysharing.KV.dto.response.FindSharingContentResponseDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class KVFeignApiService {
    @Resource
    private KVFeignApi kvFeignApi;

    public boolean addSharingContent(String uuid,String content) {
        AddSharingContentRequestDTO addSharingContentRequestDTO = AddSharingContentRequestDTO.builder()
                .uuid(uuid)
                .content(content)
                .build();
        Response<?> response = kvFeignApi.addSharingContent(addSharingContentRequestDTO);
        if (Objects.isNull(response) || !response.isSuccess()) {
            return false;
        }
        return true;
    }

    public String findSharingContent(String uuid) {
        FindSharingContentRequestDTO findSharingContentRequestDTO = FindSharingContentRequestDTO.builder()
                .uuid(uuid)
                .build();
        Response<FindSharingContentResponseDTO> response = kvFeignApi.findSharingContent(findSharingContentRequestDTO);
        if (Objects.isNull(response) || !response.isSuccess()) {
            return null;
        }
        return response.getData().getContent();
    }

    public boolean deleteSharingContent(String uuid) {
        DeleteSharingContentRequestDTO deleteSharingContentRequestDTO = DeleteSharingContentRequestDTO.builder()
                .uuid(uuid)
                .build();
        Response<?> response = kvFeignApi.deleteSharingContent(deleteSharingContentRequestDTO);
        if (Objects.isNull(response) || !response.isSuccess()) {
            return false;
        }
        return true;
    }
}
