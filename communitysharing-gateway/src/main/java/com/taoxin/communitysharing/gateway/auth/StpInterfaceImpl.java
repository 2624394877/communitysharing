package com.taoxin.communitysharing.gateway.auth;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.collection.CollUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.taoxin.communitysharing.gateway.constant.RedisKeyConstants;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;


/**
 * 自定义权限接口扩展
 * 实现STP权限框架的接口，用于获取用户权限和角色列表
 */
@Component
@Slf4j
public class StpInterfaceImpl implements StpInterface {
    @Resource
    RedisTemplate<String, String> redisTemplate;
    @Resource
    private ObjectMapper objectMapper;


    @SneakyThrows
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 获得用户的角色键值
        String userRoleValues = redisTemplate.opsForValue().get(RedisKeyConstants.getUserRolesKey(Long.valueOf(loginId.toString())));
        if (StringUtils.isBlank(userRoleValues)) {
            return null;
        }
        List<String> userRoles = objectMapper.readValue(userRoleValues, new TypeReference<List<String>>() {
        });
        if (CollUtil.isEmpty(userRoles)) {
            return null;
        }
        // 查询角色的对应权限
        // 1. 构建角色-权限键
        List<String> rolePermissionKeys = userRoles.stream().map(RedisKeyConstants::getUserPermissionsKey).toList();
        // 2. 根据键获取权限的列表集合
        List<String> rolePermissionValues = redisTemplate.opsForValue().multiGet(rolePermissionKeys);
//        log.info("==============>{}",rolePermissionValues);
        if (CollUtil.isNotEmpty(rolePermissionValues)) {
            // 获取所有的权限
            List<String> permissions = Lists.newArrayList();

            // 遍历列表集合并添加权限到permissions
            rolePermissionValues.forEach(item -> {
                try {
                    List<String> rolePermissions = objectMapper.readValue(item, new TypeReference<List<String>>() {
                    });
                    permissions.addAll(rolePermissions);
                } catch (JsonProcessingException e) {
                    log.error("数据解析错误：{}",e);
                }
            });
            return permissions;
        }

        return Collections.emptyList(); // 判断权限列表是否为空
    }

    @SneakyThrows // 当方法内部抛出受检异常时，该注解会将其包装成运行时异常抛出
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // 构建用户-角色redis键
        String userRolesKey = RedisKeyConstants.getUserRolesKey(Long.valueOf(loginId.toString()));
        // 根据id拿到用户的角色列表
        String userRoleValue = redisTemplate.opsForValue().get(userRolesKey);
//        log.info("======> 角色：{}",userRoleValue);
        if(StringUtils.isBlank(userRoleValue)) {
            return null;
        }
        // 使用 ObjectMapper 将 Redis 中存储的 JSON 字符串转换为 List<String> 类型的角色列表
        return objectMapper.readValue(userRoleValue, new TypeReference<List<String>>() {
        });
    }
}
