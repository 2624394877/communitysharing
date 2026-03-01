package com.taoxin.communitysharing.notify.business.handler;

import com.taoxin.communitysharing.common.uitl.JsonUtil;
import com.taoxin.communitysharing.notify.business.model.dto.NotifyDTO;
import io.netty.util.internal.StringUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class NotifyWebSocketHandler<T> extends TextWebSocketHandler {
    @Resource
    private RedisTemplate<String,Integer> redisTemplate;
    // 用户ID和 WebSocketSession 的映射关系
    private static final Map<Long, WebSocketSession> USER_SESSION = new ConcurrentHashMap<>();

    private static final String RedisKey = "Authorization:login:token:";

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = getUserId(session);
        log.info("==== WebSocket 连接已建立 ====> {}", session.getId());
        session.getAttributes().put("userId", userId);
        USER_SESSION.put(userId, session); // 将用户ID和 WebSocketSession 进行映射
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = getUserId(session);
        if (userId != null) {
            USER_SESSION.remove(userId);
        } // 移除用户ID和 WebSocketSession 的映射关系
    }

    public void sendToUser(NotifyDTO notifyDTO) throws IOException {
        Long userId = notifyDTO.getUserId();
        WebSocketSession session = USER_SESSION.get(userId); // 根据用户ID获取对应的 WebSocketSession
        if (session != null && session.isOpen()) { // 判断 WebSocketSession 是否存在且处于打开状态
            log.info("==== 发送通知消息给用户 {} ====> {}", userId, notifyDTO);
            session.sendMessage(new TextMessage(JsonUtil.toJsonString(notifyDTO)));
        }
    }

    private Long getUserId(WebSocketSession session) {
        URI uri = session.getUri();
        log.info("<UNK>" + uri);
        if (uri == null || uri.getQuery() == null) {
            return null;
        }
        // 截取 token 参数
        String token = Arrays.stream(uri.getQuery().split("&"))
                .filter(param -> param.startsWith("token="))
                .map(param -> param.substring("token=".length()))
                .findFirst()
                .orElse(null);
        if (StringUtil.isNullOrEmpty(token)) {
            return null;
        }

        String redisKey = RedisKey + token;
        Integer loginId = redisTemplate.opsForValue().get(redisKey);
        if (loginId == null) {
            return null;
        }
        return Long.valueOf(loginId);
    }
}
