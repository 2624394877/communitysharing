package com.taoxin.communitysharing.distributed.id.constructor.business.core;

import com.taoxin.communitysharing.distributed.id.constructor.business.core.common.Result;

public interface IDGen {
    Result get(String key);
    boolean init();
}
