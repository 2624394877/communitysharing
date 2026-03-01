package com.taoxin.communitysharing.distributed.id.constructor.business.core.common;

import com.taoxin.communitysharing.distributed.id.constructor.business.core.IDGen;

public class ZeroIDGen implements IDGen {
    @Override
    public Result get(String key) {
        return new Result(0, Status.SUCCESS);
    }

    @Override
    public boolean init() {
        return true;
    }
}
