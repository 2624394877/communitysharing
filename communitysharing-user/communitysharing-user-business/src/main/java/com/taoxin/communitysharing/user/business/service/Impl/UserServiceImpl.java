package com.taoxin.communitysharing.user.business.service.Impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.taoxin.communitysharing.common.Enums.DeletedEnum;
import com.taoxin.communitysharing.common.Enums.StatusEnum;
import com.taoxin.communitysharing.common.exception.BusinessException;
import com.taoxin.communitysharing.framework.business.context.holder.LoginUserContextHolder;
import com.taoxin.communitysharing.common.response.Response;
import com.taoxin.communitysharing.common.uitl.JsonUtil;
import com.taoxin.communitysharing.common.uitl.ParamsUtil;
import com.taoxin.communitysharing.user.business.model.vo.req.UserUpdateMailReqVo;
import com.taoxin.communitysharing.user.business.model.vo.req.UserUpdatePhoneReqVo;
import com.taoxin.communitysharing.user.business.model.vo.res.UserInfoResVo;
import com.taoxin.communitysharing.search.user.dto.requestDTO.*;
import com.taoxin.communitysharing.search.user.dto.responseDTO.FindUsersByIdResponseDTO;
import com.taoxin.communitysharing.user.business.rpc.DisIdCostructorFeignService;
import com.taoxin.communitysharing.user.business.constant.RedisKeyConstants;
import com.taoxin.communitysharing.user.business.constant.RoleConstants;
import com.taoxin.communitysharing.user.business.domain.databaseObject.RoleDo;
import com.taoxin.communitysharing.user.business.domain.databaseObject.RoleUserDo;
import com.taoxin.communitysharing.user.business.domain.databaseObject.UserDo;
import com.taoxin.communitysharing.user.business.domain.mapper.RoleDoMapper;
import com.taoxin.communitysharing.user.business.domain.mapper.RoleUserDoMapper;
import com.taoxin.communitysharing.user.business.domain.mapper.UserDoMapper;
import com.taoxin.communitysharing.user.business.enums.ResponseStatusEnum;
import com.taoxin.communitysharing.user.business.enums.SexEnums;
import com.taoxin.communitysharing.user.business.model.vo.UpdateUserInfoVO;
import com.taoxin.communitysharing.user.business.service.UserService;
import com.taoxin.communitysharing.search.user.dto.responseDTO.FindUserByIdResDTO;
import com.taoxin.communitysharing.search.user.dto.responseDTO.FindUserByPhoneResDTO;
import com.taoxin.communitysharing.search.user.dto.responseDTO.FindUsersByIdResDTO;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Preconditions;
import com.taoxin.communitysharing.user.business.rpc.OssFeignService;
import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor; // 线程池
    @Resource
    UserDoMapper userDoMapper;
    /* @Resource
    FileFeignApi fileFeignApi; */
    @Resource
    private OssFeignService ossFeignService;
    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Resource
    private RoleUserDoMapper roleUserDoMapper;
    @Resource
    private RoleDoMapper roleDoMapper;
    @Resource
    private DisIdCostructorFeignService disIdCostructorFeignService;

    // 初始化Caffeine本地缓存
    private static final Cache<Long, FindUserByIdResDTO> LOCAL_CACHE = Caffeine.newBuilder()
            .initialCapacity(10000) // 初始缓存10000个元素
            .maximumSize(10000) // 最大缓存10000个元素
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build();

    /**
     * @思路： 每个字段做验证，并进行对于操作；需要添加一个更新标记符。
     * @param updateUserInfoVO 更新用户信息
     * @return 更新结果
     */
    @Override
    public Response<?> updateUserInfo(UpdateUserInfoVO updateUserInfoVO) {
        UserDo userDo = new UserDo();
        // 获取用户id
        Long userId = LoginUserContextHolder.getUserId();
        userDo.setId(userId);
        // 设置用户更新标志
        boolean updateFlag = false;
        // 获取用户头像
        MultipartFile avatar = updateUserInfoVO.getAvatar();
        if (Objects.nonNull(avatar)) {
            // 上传头像
            // fileFeignApi.upload(avatar); // 直接调用feign接口，但是无法判断状态
            /* 调用feign封装，间接调用接口 */
            String avatarUrl = ossFeignService.upload(avatar);
            if (Objects.isNull(avatarUrl)) {
                throw new BusinessException(ResponseStatusEnum.UPLOAD_FILE_AVATAR_ERROR);
            }
            userDo.setAvatar(avatarUrl);
            updateFlag = true;
        }
        // 获取用户昵称
        String nickname = updateUserInfoVO.getNickname();
        if (StringUtils.isNotBlank(nickname)) {
            Preconditions.checkArgument(ParamsUtil.isValidNickName(nickname), ResponseStatusEnum.NICK_NAME_STR_ERROR.getErrorMessage());
            userDo.setNickname(nickname);
            updateFlag = true;
        }
        // 获取用户社区分享ID
        String communitysharingId = updateUserInfoVO.getCommunitysharingId();
        if (StringUtils.isNotBlank(communitysharingId)) {
            // 社区分享ID校验
            Preconditions.checkArgument(ParamsUtil.isValidCommunitysharingId(communitysharingId), ResponseStatusEnum.COMMUNITYSHARING_ID_ERROR.getErrorMessage());
            userDo.setCommunitysharingId(communitysharingId);
            updateFlag = true;
        }
        // 获取用户性别
        Integer sex = updateUserInfoVO.getSex();
        if (Objects.nonNull(sex)) {
            Preconditions.checkArgument(SexEnums.isValidGender(sex), ResponseStatusEnum.INVALID_GENDER.getErrorMessage());
            userDo.setSex(sex);
            updateFlag = true;
        }
        // 获取用户生日
        LocalDate birthday = updateUserInfoVO.getBirthday();
        if (Objects.nonNull(birthday)) {
            userDo.setBirthday(birthday);
            updateFlag = true;
        }
        // 获取用户简介
        String introduction = updateUserInfoVO.getIntroduction();
        if (StringUtils.isNotBlank(introduction)) {
            Preconditions.checkArgument(ParamsUtil.isValidStringLength(introduction, 100), ResponseStatusEnum.STRING_LENGTH_ERROR.getErrorMessage());
            userDo.setIntroduction(introduction);
            updateFlag = true;
        }
        // 更新用户背景图
        MultipartFile backgroundImg = updateUserInfoVO.getBackgroundImg();
        if (Objects.nonNull(backgroundImg)) {
            // 上传图片
            String backgroundImgUrl = ossFeignService.upload(backgroundImg);
            if (Objects.isNull(backgroundImgUrl)) {
                throw new BusinessException(ResponseStatusEnum.UPLOAD_FILE_BACKGROUND_ERROR);
            }
            userDo.setBackgroundImg(backgroundImgUrl);
            updateFlag = true;
        }
        // 更新用户邮箱
        String email = updateUserInfoVO.getEmail();
        UserDo userDoByEmail = null;
        if (StringUtils.isNotBlank(email)) {
            Preconditions.checkArgument(ParamsUtil.isValidEmail(email), ResponseStatusEnum.EMAIL_ERROR.getErrorMessage());
            userDoByEmail = userDoMapper.selectByEmail(email);
        }
        if (Objects.isNull(userDoByEmail)) {
            userDo.setEmail(email);
            updateFlag = true;
        }else if (Objects.equals(userDoByEmail.getId(), userId)) {
            updateFlag = true;
        }else {
            throw new BusinessException(ResponseStatusEnum.EMAIL_HAVE_EXIST);
        }
        // 根据更新标志判断是否更新
        if (updateFlag) {
            userDo.setUpdateTime(LocalDateTime.now());
            int count = userDoMapper.updateByPrimaryKeySelective(userDo);
            if (count <= 0) throw new BusinessException(ResponseStatusEnum.UPDATE_USER_FAIL);
            userDo = userDoMapper.selectByPrimaryKey(userId);
            LOCAL_CACHE.put(userId, FindUserByIdResDTO.builder()
                    .communitysharingId(userDo.getCommunitysharingId())
                    .nickname(userDo.getNickname())
                    .avatar(userDo.getAvatar())
                    .backgroundImg(userDo.getBackgroundImg())
                    .introduction(userDo.getIntroduction())
                    .isDeleted(userDo.isDeleted())
                    .build()
            );
        }
        return Response.success();
    }

    /**
     * 自动注册用户
     * @param registerUserDTO 手机号
     * @return 用户id
     *
     * @问题：
     * @Transactional注解存在的问题
     * 在该方法中如果在创建用户和角色的数据库操作之间发生异常，实际上并没有产生回滚的效果
     * @可能原因：
     * 1. @Transactional 仅在 public 方法上生效。
     * 2. 异常被捕获：如果异常在事务边界内被捕获并处理，事务可能不会回滚
     * 3. 异常处理：只有 RuntimeException 和 Error 类型的异常会触发事务回滚。
     * @该方法中 属于第二种，发生异常时被自定义的全局异常类捕获和处理，导致事务无法回滚
     * @解决方法： 使用编程式事务(注意删除注解避免混用)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Long> registerUser(RegisterUserDTO registerUserDTO) {
        String phone = registerUserDTO.getPhone();
        UserDo userDo = userDoMapper.selectByPhone(phone);
        if (Objects.nonNull(userDo)) {
            return Response.success(userDo.getId());
        }
        // 获取redis全局自增communitysharingId
//        Long communitysharingId = redisTemplate.opsForValue().increment(RedisKeyConstants.COMMUNITYSHARING_ID_GENERATOR);
        String communitysharingId = disIdCostructorFeignService.getSegmentId();
        // 将id存储到redis
        redisTemplate.opsForValue().set(RedisKeyConstants.COMMUNITYSHARING_ID_GENERATOR,communitysharingId);
        // 生成主键id
        String id = disIdCostructorFeignService.getUserId();
        log.info("=======> 生成用户id：{}",Long.valueOf(id));
        // 创建用户
        UserDo createUser = UserDo.builder()
                .id(Long.valueOf(id))
                .phone(phone)
                .communitysharingId(communitysharingId)
                .nickname("用户" + communitysharingId)
                .status(StatusEnum.ENABLED.getCode())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDeleted(DeletedEnum.NO.getValue())
                .build();
        // 插入数据库
        userDoMapper.insert(createUser);
        // 获取用户ID，为了在user_roles表中插入数据
        Long userId  = createUser.getId();
        RoleUserDo roleUserDo = RoleUserDo.builder()
                .userId(userId)
                .roleId(RoleConstants.COMMON_USER_ROLE_ID)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDeleted(DeletedEnum.NO.getValue())
                .build();
        // 将关联的用户角色插入数据关联表中
        roleUserDoMapper.insert(roleUserDo);
        // 以上所有数据库操作都完成

        // 将用户的角色ID放入到Redis中
        RoleDo roleDo = roleDoMapper.selectByPrimaryKey(RoleConstants.COMMON_USER_ROLE_ID);
        List<String> roles = Lists.newArrayList();
        roles.add(roleDo.getRoleKey());
        String key = RedisKeyConstants.getUserRolesKey(userId);
        redisTemplate.opsForValue().set(key, JsonUtil.toJsonString(roles));
        return Response.success(userId);
    }

    @Override
    public Response<FindUserByPhoneResDTO> findUserByPhone(FindUserByPhoneDTO findUserByPhoneDTO) {
        String phone = findUserByPhoneDTO.getPhone();
        UserDo userDo = userDoMapper.selectByPhone(phone);
        if (Objects.isNull(userDo)) {
            throw new BusinessException(ResponseStatusEnum.USER_NOT_EXIST);
        }
        FindUserByPhoneResDTO findUserByPhoneResDTO = FindUserByPhoneResDTO.builder()
                .id(userDo.getId())
                .password(userDo.getPassword())
                .build();
        return Response.success(findUserByPhoneResDTO);
    }

    @Override
    public Response<Long> findUserIdByEmail(FindUserByEmailDTO findUserByEmailDTO) {
        String email = findUserByEmailDTO.getEmail();
        UserDo userDo = userDoMapper.selectByEmail(email);
        if (Objects.isNull(userDo)) {
            throw new BusinessException(ResponseStatusEnum.EMAIL_NOT_EXIST);
        }
        return Response.success(userDo.getId());
    }

    @Override
    public Response<?> PasswordUpdate(UpdateUserPasswordDTO updateUserPasswordDTO) {
        String EncodePassword = updateUserPasswordDTO.getEncodePassword();
        Long userId = LoginUserContextHolder.getUserId();
        log.info("用户{}修改密码", userId);
        UserDo userDo = UserDo.builder()
                .id(userId)
                .password(EncodePassword)
                .updateTime(LocalDateTime.now())
                .build();
        int update = userDoMapper.updateByPrimaryKeySelective(userDo);
        if (update <= 0) {
            return Response.fail(ResponseStatusEnum.UPDATE_USER_PASSWORD_ERROR);
        }
        return Response.success();
    }

    /**
     * 根据用户id查询用户信息
     * @param findUserByIdDTO 用户idDTO请求实体
     * @return 用户信息
     * @思路：
     * 1. 先从本地缓存中获取数据，如果存在则直接返回
     * 2. 如果不存在则从Redis中获取数据，如果存在则返回
     * 3. 如果不存在则从数据库中获取数据，并返回，并缓存到Redis中
     */
    @Override
    public Response<FindUserByIdResDTO> findUserById(FindUserByIdDTO findUserByIdDTO) {
        Long userId = findUserByIdDTO.getUserId();
        // 在本地缓存中获取数据
        FindUserByIdResDTO findUserByIdResDTOLocalCache = LOCAL_CACHE.getIfPresent(userId);
        if (Objects.nonNull(findUserByIdResDTOLocalCache)) { // 本地缓存中存在数据
            log.info("===> 从本地缓存中获取数据:{}", findUserByIdResDTOLocalCache); // 输出日志
            return Response.success(findUserByIdResDTOLocalCache);
        }

        // 构建缓存键
        String userInfoRedisKey = RedisKeyConstants.getUserInfoKey(userId);

        // 获取缓存数据
        FindUserByIdResDTO findUserByIdRes = null;
        String userInfoRedisValue = (String) redisTemplate.opsForValue().get(userInfoRedisKey);
        if (StringUtils.isNotBlank(userInfoRedisValue)) {
            // 将json数据转为对象
            FindUserByIdResDTO findUserByIdResDTO = JsonUtil.parseObject(userInfoRedisValue, FindUserByIdResDTO.class);
            // 调用异步线程将数据写入缓存
            threadPoolTaskExecutor.submit(() -> {
                // 将数据写入本地缓存
                LOCAL_CACHE.put(userId, findUserByIdResDTO);
            });
            findUserByIdRes = findUserByIdResDTO;
            findUserByIdRes.setId(null);
            return Response.success(findUserByIdRes);
        }

        UserDo user = userDoMapper.selectByPrimaryKey(userId);
        if (Objects.isNull(user)) {
            threadPoolTaskExecutor.execute(() -> {
                long expireSec = 60 + RandomUtil.randomInt(60); // 随机60秒到120秒的过期时间 防止缓存雪崩
                redisTemplate.opsForValue().set(userInfoRedisKey, "null", expireSec, TimeUnit.SECONDS); // 缓存数据不存在，则将数据写入缓存 防止缓存穿透
                throw new BusinessException(ResponseStatusEnum.USER_NOT_EXIST);
            });// 数据不存在，则写入null到缓存
        }
        FindUserByIdResDTO findUserByIdResDTORedis = FindUserByIdResDTO.builder()
                .id(user.getId())
                .communitysharingId(user.getCommunitysharingId())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .backgroundImg(user.getBackgroundImg())
                .introduction(user.getIntroduction())
                .isDeleted(user.isDeleted())
                .build();
        threadPoolTaskExecutor.submit(() -> {
            long expireSec = 60*60*24 + RandomUtil.randomInt(60*60*24); // 1天 + 随机60秒到24*60*60秒的过期时间 防止缓存雪崩
            redisTemplate.opsForValue().set(userInfoRedisKey, JsonUtil.toJsonString(findUserByIdResDTORedis), expireSec, TimeUnit.SECONDS);
        });// 将数据存入缓存
        FindUserByIdResDTO findUserByIdResDto = FindUserByIdResDTO.builder()
                .communitysharingId(user.getCommunitysharingId())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .backgroundImg(user.getBackgroundImg())
                .introduction(user.getIntroduction())
                .isDeleted(user.isDeleted())
                .build();
        return Response.success(findUserByIdResDto);
    }

    @Override
    public Response<FindUsersByIdResponseDTO> findUsersById(FindUsersByIdDTO findUsersByIdDTO) {
        List<Long> userIds = findUsersByIdDTO.getUsersId();

        // 构建缓存键
        List<String> userInfoRedisKeys = userIds.stream()
                .map(userId -> RedisKeyConstants.getUserInfoKey(userId))
                .toList();
        // 获取缓存数据
        List<Object> userInfoRedisValues = redisTemplate.opsForValue().multiGet(userInfoRedisKeys);
        if (CollUtil.isNotEmpty(userInfoRedisValues)) {
            // 过滤掉null的数据
            userInfoRedisValues = userInfoRedisValues.stream()
                    .filter(Objects::nonNull)
                    .toList();
        }

        List<FindUsersByIdResDTO> findUsersByIdResDTOS = Lists.newArrayList(); // 创建一个空的列表
        // 将过滤后的数据转为对象
        if (CollUtil.isNotEmpty(userInfoRedisValues)) {
            findUsersByIdResDTOS = userInfoRedisValues.stream()
                    .map(userInfoRedisValue -> JsonUtil.parseObject(String.valueOf(userInfoRedisValue), FindUsersByIdResDTO.class))
                    .toList();
        }
        FindUsersByIdResponseDTO findUsersByIdResponseDTO = null;
        if(CollUtil.size(userIds) == CollUtil.size(findUsersByIdResDTOS)){
            // 数据全部存在缓存中
            findUsersByIdResponseDTO = FindUsersByIdResponseDTO.builder()
                    .usersInfo(findUsersByIdResDTOS)
                    .build();
            return Response.success(findUsersByIdResponseDTO);
        };

        // 数据并没有全部存在缓存中 需要从数据库中获取数据，或者一个数据都没有
        List<Long> usersIdQuery = null;
        // 先判断过滤的数据是否为空
        if (CollUtil.isEmpty(findUsersByIdResDTOS)) {
            // 不为空说明，数据不全
            Map<Long,FindUsersByIdResDTO> map = findUsersByIdResDTOS.stream()
                    .collect(Collectors.toMap(FindUsersByIdResDTO::getId, o-> o));
            // 筛选出不存在的id
            usersIdQuery = userIds.stream()
                    .filter(userId -> !map.containsKey(userId))
                    .toList();
        } else {
            // 一个数据都没有 需要从数据库中获取所有的userIds数据
            usersIdQuery = userIds;
        }

        // 获取数据库中的数据
        List<UserDo> userDos = userDoMapper.selectById(usersIdQuery);
        List<FindUsersByIdResDTO> findUsersByIdResDTOSQuery = null;
        if (CollUtil.isNotEmpty(userDos)) {
            findUsersByIdResDTOSQuery = userDos.stream()
                    .map(userDo -> FindUsersByIdResDTO.builder()
                            .id(userDo.getId())
                            .communitysharingId(userDo.getCommunitysharingId())
                            .nickname(userDo.getNickname())
                            .avatar(userDo.getAvatar())
                            .introduction(userDo.getIntroduction())
                            .build())
                    .toList();
            // 缓存数据
            List<FindUsersByIdResDTO> findUsersByIdResDTOSPush = findUsersByIdResDTOSQuery;
            threadPoolTaskExecutor.submit(() -> {
                Map<Long, FindUsersByIdResDTO> map = findUsersByIdResDTOSPush.stream()
                        .collect(Collectors.toMap(FindUsersByIdResDTO::getId, o-> o));

                // pipeline操作
                redisTemplate.executePipelined(new SessionCallback() {
                    @Override
                    public Object execute(RedisOperations operations) throws DataAccessException {
                        for (UserDo userDo : userDos) {
                            String userInfoRedisKey = RedisKeyConstants.getUserInfoKey(userDo.getId());
                            // 在map集合中获取数据，并转为json字符串
                            FindUsersByIdResDTO findUsersByIdResDTO = map.get(userDo.getId());
                            String value = JsonUtil.toJsonString(findUsersByIdResDTO);

                            long expireSec = 60 * 60 * 24 + RandomUtil.randomInt(60 * 60 * 24);
                            operations.opsForValue().set(userInfoRedisKey, value, expireSec, TimeUnit.SECONDS);
                        }
                        return null;
                    }
                });
            });
        }

        List<FindUsersByIdResDTO> findUsersByIdResDTOList = null;
        if (CollUtil.isNotEmpty(findUsersByIdResDTOSQuery)) {
            findUsersByIdResDTOList = userDos.stream()
                    .map(userDo -> FindUsersByIdResDTO.builder()
                            .id(userDo.getId())
                            .communitysharingId(userDo.getCommunitysharingId())
                            .nickname(userDo.getNickname())
                            .avatar(userDo.getAvatar())
                            .introduction(userDo.getIntroduction())
                            .build())
                    .toList();
            findUsersByIdResponseDTO = FindUsersByIdResponseDTO.builder()
                    .usersInfo(findUsersByIdResDTOList)
                    .build();
        }
        return Response.success(findUsersByIdResponseDTO);
    }

    @Override
    public Response<FindUserByIdResDTO> findUser() {
        Long userId = LoginUserContextHolder.getUserId();

        // 在本地缓存中获取数据
        FindUserByIdResDTO findUserByIdResDTOLocalCache = LOCAL_CACHE.getIfPresent(userId);
        if (Objects.nonNull(findUserByIdResDTOLocalCache)) { // 本地缓存中存在数据
            log.info("===> 从本地缓存中获取数据:{}", findUserByIdResDTOLocalCache); // 输出日志
            return Response.success(findUserByIdResDTOLocalCache);
        }
        String userInfoRedisKey = RedisKeyConstants.getUserInfoKey(userId);
        // 获取缓存数据
        FindUserByIdResDTO findUserByIdRes = null;
        String userInfoRedisValue = (String) redisTemplate.opsForValue().get(userInfoRedisKey);
        if (StringUtils.isNotBlank(userInfoRedisValue)) {
            // 将json数据转为对象
            FindUserByIdResDTO findUserByIdResDTO = JsonUtil.parseObject(userInfoRedisValue, FindUserByIdResDTO.class);
            // 调用异步线程将数据写入缓存
            threadPoolTaskExecutor.submit(() -> {
                // 将数据写入本地缓存
                LOCAL_CACHE.put(userId, findUserByIdResDTO);
            });
            findUserByIdRes = findUserByIdResDTO;
            return Response.success(findUserByIdRes);
        }
        // 数据不存在, 则从数据库中获取数据
        UserDo user = userDoMapper.selectByPrimaryKey(userId);
        if (Objects.isNull(user)) {
            threadPoolTaskExecutor.execute(() -> {
                // 数据不存在，则写入null到缓存
                long expireSec = 60 + RandomUtil.randomInt(60); // 随机60秒到120秒的过期时间 防止缓存雪崩
                redisTemplate.opsForValue().set(userInfoRedisKey, "null", expireSec, TimeUnit.SECONDS); // 缓存数据不存在，则将数据写入缓存 防止缓存穿透
                throw new BusinessException(ResponseStatusEnum.USER_NOT_EXIST);
            });
        }
        FindUserByIdResDTO findUserByIdResDTORedis = FindUserByIdResDTO.builder()
                .id(user.getId())
                .communitysharingId(user.getCommunitysharingId())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .backgroundImg(user.getBackgroundImg())
                .introduction(user.getIntroduction())
                .isDeleted(user.isDeleted())
                .build();
        threadPoolTaskExecutor.submit(() -> {
            // 将数据存入缓存
            long expireSec = 60*60*24 + RandomUtil.randomInt(60*60*24); // 1天 + 随机60秒到24*60*60秒的过期时间 防止缓存雪崩
            redisTemplate.opsForValue().set(userInfoRedisKey, JsonUtil.toJsonString(findUserByIdResDTORedis), expireSec, TimeUnit.SECONDS);
        });
        FindUserByIdResDTO findUserByIdResDto = FindUserByIdResDTO.builder()
                .communitysharingId(user.getCommunitysharingId())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .backgroundImg(user.getBackgroundImg())
                .introduction(user.getIntroduction())
                .isDeleted(user.isDeleted())
                .build();
        return Response.success(findUserByIdResDto);
    }

    @Override
    public Response<UserInfoResVo> getUserInfo() {
        Long userId = LoginUserContextHolder.getUserId();
        UserDo user = userDoMapper.selectByPrimaryKey(userId);
        if (Objects.isNull(user)) {
            throw new BusinessException(ResponseStatusEnum.USER_NOT_EXIST);
        }
        UserInfoResVo userInfoResVo = UserInfoResVo.builder()
                .communitysharingId(user.getCommunitysharingId())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .birthday(user.getBirthday())
                .backgroundImg(user.getBackgroundImg())
                .email(user.getEmail())
                .phone(user.getPhone())
                .sex(user.getSex())
                .introduction(user.getIntroduction())
                .createTime(user.getCreateTime())
                .isDeleted(user.isDeleted())
                .build();
        return Response.success(userInfoResVo);
    }

    @Override
    public Response<?> updateUserMail(UserUpdateMailReqVo userUpdateMailReqVo) {
        Long userId = LoginUserContextHolder.getUserId();
        String mail = userUpdateMailReqVo.getEmail();
        Preconditions.checkArgument(ParamsUtil.isValidEmail(mail), ResponseStatusEnum.EMAIL_FORMAT_ERROR.getErrorMessage());
        if (Objects.nonNull(userDoMapper.selectByEmail(mail))) {
            throw new BusinessException(ResponseStatusEnum.EMAIL_HAVE_EXIST);
        }
        String code = userUpdateMailReqVo.getCode();
        if (StringUtils.isBlank(code)) { // 判断验证码是否为空，空则抛出参数无效异常
            throw new BusinessException(ResponseStatusEnum.PARAMS_ERROR);
        }
        String key = RedisKeyConstants.getVerificationCodeKey(mail);
        String redisCode = (String) redisTemplate.opsForValue().get(key);
        if (!StringUtils.equals(code, redisCode)) { // 验证码不一致则抛出验证码错误异常
            throw new BusinessException(ResponseStatusEnum.VERIFICATION_CODE_ERROR);
        }
        UserDo user = UserDo.builder()
                .id(userId)
                .email(mail)
                .build();
        int update = userDoMapper.updateByPrimaryKeySelective(user);
        if (update <= 0) throw new BusinessException(ResponseStatusEnum.UPDATE_USER_MAIL_ERROR);
        return Response.success();
    }

    @Override
    public Response<?> updateUserPhone(UserUpdatePhoneReqVo userUpdatePhoneReqVo) {
        Long userId = LoginUserContextHolder.getUserId();
        String phone = userUpdatePhoneReqVo.getPhone();
        Preconditions.checkArgument(ParamsUtil.isValidPhoneNumber(phone), ResponseStatusEnum.PHONE_FORMAT_ERROR.getErrorMessage());
        if (Objects.nonNull(userDoMapper.selectByPhone(phone))) {
            throw new BusinessException(ResponseStatusEnum.PHONE_HAVE_EXIST);
        }
        String code = userUpdatePhoneReqVo.getCode();
        if (StringUtils.isBlank(code)) { // 判断验证码是否为空，空则抛出参数无效异常
            throw new BusinessException(ResponseStatusEnum.PARAMS_ERROR);
        }
        String key = RedisKeyConstants.getVerificationCodeKey(phone);
        String redisCode = (String) redisTemplate.opsForValue().get(key);
        if (!StringUtils.equals(code, redisCode)) { // 验证码不一致则抛出验证码错误异常
            throw new BusinessException(ResponseStatusEnum.VERIFICATION_CODE_ERROR);
        }
        UserDo user = UserDo.builder()
                .id(userId)
                .phone(phone)
                .build();
        int update = userDoMapper.updateByPrimaryKeySelective(user);
        if (update <= 0) throw new BusinessException(ResponseStatusEnum.UPDATE_USER_PHONE_ERROR);
        return Response.success();
    }
}
