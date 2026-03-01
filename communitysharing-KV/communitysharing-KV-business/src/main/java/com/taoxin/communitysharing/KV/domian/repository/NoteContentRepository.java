package com.taoxin.communitysharing.KV.domian.repository;

import com.taoxin.communitysharing.KV.domian.dataobject.NoteContentDO;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.UUID;

public interface NoteContentRepository extends CassandraRepository<NoteContentDO, UUID> {
}
