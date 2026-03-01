package com.taoxin.communitysharing.KV;

import com.taoxin.communitysharing.KV.domian.dataobject.NoteContentDO;
import com.taoxin.communitysharing.KV.domian.repository.NoteContentRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@Slf4j
@SpringBootTest
public class TestCassandra {
    @Resource
    private NoteContentRepository noteContentRepository;
    @Test
    void InsertTable() { // 插入数据，表不存在时，自动创建表
        NoteContentDO noteContentDO = NoteContentDO.builder()
                .Id(UUID.randomUUID())
                .content("笔记内容。")
                .build();
        noteContentRepository.save(noteContentDO);
    }

    @Test
    void QueryTable() { // 查询数据
        NoteContentDO noteContentDO = noteContentRepository.findById(UUID.fromString("75876e33-1019-46f4-acfb-9a047b450590")).get();
        log.info("查询结果：{}", noteContentDO);
    }

    @Test
    void DeleteTable() {
        noteContentRepository.deleteById(UUID.fromString("f8d73655-d975-4c3b-ad26-6b1d28d16fc0"));
        log.info("删除成功");
    }
}
