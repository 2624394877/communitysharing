package com.taoxin.communitysharing.KV.service;

import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.KV.dto.request.AddSharingContentRequestDTO;
import com.taoxin.communitysharing.KV.dto.request.DeleteSharingContentRequestDTO;
import com.taoxin.communitysharing.KV.dto.request.FindSharingContentRequestDTO;

public interface SharingContent {
    Response<?> addSharingContent(AddSharingContentRequestDTO requestDTO);

    Response<?> getSharingContent(FindSharingContentRequestDTO requestDTO);

    Response<?> deleteSharingContent(DeleteSharingContentRequestDTO requestDTO);
}
