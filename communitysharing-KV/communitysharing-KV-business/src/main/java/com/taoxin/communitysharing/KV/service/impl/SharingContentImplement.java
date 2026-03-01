package com.taoxin.communitysharing.KV.service.impl;

import com.taoxin.communitysharing.KV.domian.dataobject.NoteContentDO;
import com.taoxin.communitysharing.KV.domian.repository.NoteContentRepository;
import com.taoxin.communitysharing.KV.enums.ResponseStatusEnum;
import com.taoxin.communitysharing.KV.service.SharingContent;
import com.taoxin.communitysharing.common.exception.BusinessException;
import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.KV.dto.request.AddSharingContentRequestDTO;
import com.taoxin.communitysharing.KV.dto.request.DeleteSharingContentRequestDTO;
import com.taoxin.communitysharing.KV.dto.request.FindSharingContentRequestDTO;
import com.taoxin.communitysharing.KV.dto.response.FindSharingContentResponseDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class SharingContentImplement implements SharingContent {
    @Resource
    private NoteContentRepository noteContentRepository;
    @Override
    public Response<?> addSharingContent(AddSharingContentRequestDTO requestDTO) {
        // 获取id
        String id = requestDTO.getUuid();
        String content = requestDTO.getContent();
        // 构建添加内容
        NoteContentDO noteContentDO = NoteContentDO.builder()
                .Id(UUID.fromString(id))
                .content(content)
                .build();
        noteContentRepository.save(noteContentDO);
        return Response.success();
    }

    @Override
    public Response<?> getSharingContent(FindSharingContentRequestDTO requestDTO) {
        String id = requestDTO.getUuid();
        // 获取笔记内容
        /**
         * Optional<T>: java8新特性 用于封装数据 安全地处理可能不存在的对象实例 明确表示这个值可能是"有值"或"无值"
         */
        Optional<NoteContentDO> noteContentDO = noteContentRepository.findById(UUID.fromString(id));

        if (!noteContentDO.isPresent()) {
            throw new BusinessException(ResponseStatusEnum.NOT_FOUND_CONTENT);
        }

        NoteContentDO noteContent = noteContentDO.get();
        FindSharingContentResponseDTO responseDTO = FindSharingContentResponseDTO.builder()
                .content(noteContent.getContent())
                .build();
        return Response.success(responseDTO);
    }

    @Override
    public Response<?> deleteSharingContent(DeleteSharingContentRequestDTO requestDTO) {
        String id = requestDTO.getUuid();
        noteContentRepository.deleteById(UUID.fromString(id));
        return Response.success();
    }
}
