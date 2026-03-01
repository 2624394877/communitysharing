package com.taoxin.communitysharing.distributed.id.constructor.business.service;

import com.alibaba.druid.pool.DruidDataSource;
import com.taoxin.communitysharing.distributed.id.constructor.business.core.IDGen;
import com.taoxin.communitysharing.distributed.id.constructor.business.core.common.PropertyFactory;
import com.taoxin.communitysharing.distributed.id.constructor.business.core.common.Result;
import com.taoxin.communitysharing.distributed.id.constructor.business.core.common.ZeroIDGen;
import com.taoxin.communitysharing.distributed.id.constructor.business.core.segment.SegmentIDGenImpl;
import com.taoxin.communitysharing.distributed.id.constructor.business.core.segment.dao.IDAllocDao;
import com.taoxin.communitysharing.distributed.id.constructor.business.core.segment.dao.impl.IDAllocDaoImpl;
import com.taoxin.communitysharing.distributed.id.constructor.business.constant.Constants;
import com.taoxin.communitysharing.distributed.id.constructor.business.exception.InitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Properties;

@Service("SegmentService")
public class SegmentService {
    private Logger logger = LoggerFactory.getLogger(SegmentService.class);

    private IDGen idGen;
    private DruidDataSource dataSource;

    public SegmentService() throws SQLException, InitException {
        Properties properties = PropertyFactory.getProperties();
        boolean flag = Boolean.parseBoolean(properties.getProperty(Constants.LEAF_SEGMENT_ENABLE, "true"));
        if (flag) {
            // Config dataSource
            dataSource = new DruidDataSource();
            dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
            dataSource.setUrl(properties.getProperty(Constants.LEAF_JDBC_URL));
            dataSource.setUsername(properties.getProperty(Constants.LEAF_JDBC_USERNAME));
            dataSource.setPassword(properties.getProperty(Constants.LEAF_JDBC_PASSWORD));
            dataSource.setValidationQuery("select 1");
            dataSource.init();

            // Config Dao
            IDAllocDao dao = new IDAllocDaoImpl(dataSource);

            // Config ID Gen
            idGen = new SegmentIDGenImpl();
            ((SegmentIDGenImpl) idGen).setDao(dao);
            if (idGen.init()) {
                logger.info("Segment Service Init Successfully");
            } else {
                throw new InitException("Segment Service Init Fail");
            }
        } else {
            idGen = new ZeroIDGen();
            logger.info("Zero ID Gen Service Init Successfully");
        }
    }

    public Result getId(String key) {
        return idGen.get(key);
    }

    public SegmentIDGenImpl getIdGen() {
        if (idGen instanceof SegmentIDGenImpl) {
            return (SegmentIDGenImpl) idGen;
        }
        return null;
    }
}
