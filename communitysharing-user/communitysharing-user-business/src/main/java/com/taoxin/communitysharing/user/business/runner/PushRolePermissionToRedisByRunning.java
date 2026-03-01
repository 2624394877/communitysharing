package com.taoxin.communitysharing.user.business.runner;

import cn.hutool.core.collection.CollUtil;
import com.taoxin.communitysharing.common.uitl.JsonUtil;
import com.taoxin.communitysharing.user.business.constant.RedisKeyConstants;
import com.taoxin.communitysharing.user.business.domain.databaseObject.PermissionDo;
import com.taoxin.communitysharing.user.business.domain.databaseObject.RoleDo;
import com.taoxin.communitysharing.user.business.domain.databaseObject.RoleRermissionDo;
import com.taoxin.communitysharing.user.business.domain.databaseObject.RoleUserDo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.taoxin.communitysharing.user.business.domain.mapper.PermissionDoMapper;
import com.taoxin.communitysharing.user.business.domain.mapper.RoleDoMapper;
import com.taoxin.communitysharing.user.business.domain.mapper.RoleRermissionDoMapper;
import com.taoxin.communitysharing.user.business.domain.mapper.RoleUserDoMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Slf4j
public class PushRolePermissionToRedisByRunning implements ApplicationRunner {
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private RoleDoMapper roleDoMapper; // 角色
    @Resource
    private PermissionDoMapper permissionDoMapper; // 权限
    @Resource
    private RoleRermissionDoMapper roleRermissionDoMapper; // 角色权限关系
    @Resource
    private RoleUserDoMapper roleUserDoMapper;

    private static final String LOCK_KEY = "push.permission.flag";

    /**
     *
     * @问题
     * 由于项目是分布式微服务框架，因此每个服务在启动时都需要将数据库角色权限关系推到Redis中
     * 会造成多次同步的问题，我们只需要其中一个服务进行同步。
     * @解决方案： Redis分布式锁
     * @实现思路：
     * 使用Redis的SET key value NX命令，当key存在表示已经加锁，返回0；当key不存在表示没有加锁，返回1。
     * 多个服务访问锁时，只有先到的加锁，并同步数据。
     * 设置锁的过期时间，防止锁长期不释放，一般为12小时。
     */
    @Override
    public void run(ApplicationArguments args) {
        log.info("== 项目启动 ==> 将数据库角色权限关系推送到Redis中");
        try {
            boolean lock = redisTemplate.opsForValue().setIfAbsent(LOCK_KEY, "1", Duration.ofHours(12)); // 获取锁

            if (!lock) { // 锁不存在
                log.info("== 项目启动 ==> 锁已存在，正在等待锁释放");
                return; // 直接返回，不需要要进行下面的逻辑
            }

            // 启动时将角色权限关系推送到Redis中(同步数据)
            // 获取所有启用的角色
            List<RoleDo> roleList = roleDoMapper.selectEnabledRolesList();
            // 获取角色id-权限id的关系
            List<Long> roleIds = roleList.stream()
                    .map(RoleDo::getId)
                    .toList(); // 获取角色id
            log.info("== 项目启动 ==> 获取所有启用的角色: {}", roleIds);
            List<RoleRermissionDo> roleRermissionList = roleRermissionDoMapper.selectByRoleIds(roleIds); // 根据角色id，批量获取角色的对应权限
            Map<Long, List<Long>> roleRermissionMap = roleRermissionList.stream().collect(
                    // 获取角色id
                    Collectors.groupingBy(RoleRermissionDo::getRoleId,
                            Collectors.mapping(RoleRermissionDo::getPermissionId, Collectors.toList())
                            )
            ); // 按照角色 id 分组，获取角色id对应的权限id
            // 获取角色-权限的启用状态
            List<PermissionDo> permissionList = permissionDoMapper.selectEnabledList(); // 获取所有启用的权限
            Map<Long,PermissionDo> permissionMap = permissionList.stream().collect(Collectors.toMap(PermissionDo::getId,
                    permissionDo -> permissionDo)); // 获取权限id-权限关系
            // 构建角色-权限的关系集合
            Map<String, List<String>> roleIdpermissionDoMap = Maps.newHashMap(); // 角色id-权限关系集合
            roleList.forEach(roleDo -> { // 遍历角色
                Long roleId = roleDo.getId();
                List<Long> permissionIds = roleRermissionMap.get(roleId); // 获取角色id对应的权限id
                if (CollUtil.isNotEmpty(permissionIds)){ // 角色id对应的权限id不为空
                    List<String> permissionkeyList = Lists.newArrayList(); // 创建角色id对应的权限关系集合
                    permissionIds.forEach(permissionId -> {
                        PermissionDo permissionDo = permissionMap.get(permissionId); // 获取权限id对应的权限
                        if (Objects.nonNull(permissionDo)) { // 权限id对应的权限不为空
                            permissionkeyList.add(permissionDo.getPermissionKey());
                        }
                    });
                    String roleKey = roleDo.getRoleKey();
                    roleIdpermissionDoMap.put(roleKey, permissionkeyList); // 角色id对应的权限关系集合
                }
            });

            // 将获取到的集合存到redis
            roleIdpermissionDoMap.forEach((roleKey, permissionDoList) -> {
                redisTemplate.opsForValue().set(RedisKeyConstants.getUserPermissionsKey(roleKey), JsonUtil.toJsonString(permissionDoList));
            });
            List<RoleUserDo> roleUserList = roleUserDoMapper.selectAll();
            Map<Long, List<String>> roleUserMap = new HashMap<>();
            roleUserList.forEach(roleUserDo -> {
                Long userId = roleUserDo.getUserId();
                List<String> roleKeys = roleList.stream()
                        .filter(roleDo -> roleDo.getId().equals(roleUserDo.getRoleId()))
                        .map(RoleDo::getRoleKey)
                        .toList();
                roleUserMap.put(userId, roleKeys);
            });
            redisTemplate.executePipelined(new SessionCallback() {
                @Override
                public Object execute(RedisOperations operations) throws DataAccessException {
                    roleUserMap.forEach((userId, roleKeys) -> {
                        operations.opsForValue().set(RedisKeyConstants.getUserRolesKey(userId), JsonUtil.toJsonString(roleKeys));
                    });
                    return null;
                }
            });
            log.info("== 项目启动 ==> 角色权限关系推送到Redis中完成");
        } catch (Exception e) {
            log.error("== 项目启动 ==> 角色权限关系推送到Redis中失败: {}", e);
        }
    }
}
