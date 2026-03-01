package com.taoxin.communitysharing.distributed.id.constructor.business.service;

import com.taoxin.communitysharing.distributed.id.constructor.business.core.IDGen;
import com.taoxin.communitysharing.distributed.id.constructor.business.core.common.PropertyFactory;
import com.taoxin.communitysharing.distributed.id.constructor.business.core.common.Result;
import com.taoxin.communitysharing.distributed.id.constructor.business.core.common.ZeroIDGen;
import com.taoxin.communitysharing.distributed.id.constructor.business.constant.Constants;
import com.taoxin.communitysharing.distributed.id.constructor.business.exception.InitException;
import com.taoxin.communitysharing.distributed.id.constructor.business.core.snowflake.SnowflakeIDGenImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service("SnowflakeService")
public class SnowflakeService {
    private Logger logger = LoggerFactory.getLogger(SnowflakeService.class);

    private IDGen idGen;

    public SnowflakeService() throws InitException {
        Properties properties = PropertyFactory.getProperties();
        boolean flag = Boolean.parseBoolean(properties.getProperty(Constants.LEAF_SNOWFLAKE_ENABLE, "true"));
        if (flag) {
            String zkAddress = properties.getProperty(Constants.LEAF_SNOWFLAKE_ZK_ADDRESS);
            int port = Integer.parseInt(properties.getProperty(Constants.LEAF_SNOWFLAKE_PORT));
            idGen = new SnowflakeIDGenImpl(zkAddress, port);
            if(idGen.init()) {
                logger.info("Snowflake Service Init Successfully");
            } else {
                throw new InitException("Snowflake Service Init Fail");
            }
        } else {
            idGen = new ZeroIDGen();
            logger.info("Zero ID Gen Service Init Successfully");
        }
    }

    public Result getId(String key) {
        return idGen.get(key);
    }
}
