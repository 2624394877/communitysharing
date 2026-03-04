package com.taoxin.communitysharing.user.business;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.taoxin.communitysharing.user.business.domain.databaseObject.UserDo;
import com.taoxin.communitysharing.user.business.domain.mapper.UserDoMapper;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@SpringBootTest
@Slf4j
public class DruidTest {
//    @Resource
//    UserDoMapper userDoMapper;
//    @Resource
//    private RedisTemplate<String, Object> redisTemplate;

//    @Test
//    void selectUser() {
//        UserDo userDo = userDoMapper.selectByPrimaryKey(8L);
//        log.info("用户信息：{}", userDo);
//    }

//    @Test
//    @SneakyThrows // 忽略方法抛出的异常
//    void testDruid() {
//        // 使用Druid的内置加密工具加密密码
//        String password = "123456";
//        String[] arr = ConfigTools.genKeyPair(1024); // 生成公钥和私钥
//        log.info("公钥：{}", arr[0]);
//        log.info("私钥：{}", arr[1]);
//        // 进行加密
//        String cipherText = ConfigTools.encrypt(arr[0], password);
//        log.info("密文：{}", cipherText);
//    }

//    @Test
//    @SneakyThrows // 忽略方法抛出的异常
//    void Test() {
//        List<Object> objs = Lists.newArrayList();
//        objs.add(null);
//        objs.add(null);
//        objs.add(null);
//        if (CollUtil.isNotEmpty(objs)) {
//            log.info("集合不会空：{}", objs); // 集合不会空：[null, null, null]
//            objs = objs.stream().filter(Objects::nonNull).toList();
//        } else {
//            log.info("集合为空：{}", objs);
//        }
//        if (CollUtil.isNotEmpty(objs)) log.info("集合不会空：{}", objs);
//        else log.info("(过滤后)集合为空：{}", objs);
//    }
}
