package com.taoxin.communitysharing.distributed.id.constructor.business.core.snowflake.exception;

public class ClockGoBackException extends RuntimeException {
    public ClockGoBackException(String message) {
        super(message);
    }
}
