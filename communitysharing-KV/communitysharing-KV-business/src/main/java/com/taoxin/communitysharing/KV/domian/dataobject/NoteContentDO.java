package com.taoxin.communitysharing.KV.domian.dataobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.UUID;

@Data
@Table("note_content")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NoteContentDO {
    @PrimaryKey(value = "id")
    private UUID Id;

    private String content;
}
