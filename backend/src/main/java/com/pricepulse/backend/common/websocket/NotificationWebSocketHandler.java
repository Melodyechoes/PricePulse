package com.pricepulse.backend.common.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    // 保存所有连接的 Session，key 为 userId
    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 连接建立后
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 从 URL 参数中获取 userId
        String userIdStr = (String) session.getAttributes().get("userId");
        if (userIdStr != null) {
            try {
                Long userId = Long.parseLong(userIdStr);
                sessions.put(userId, session);
                log.info("用户 {} 已连接 WebSocket，当前在线人数：{}", userId, sessions.size());
            } catch (NumberFormatException e) {
                log.warn("无效的 userId: {}", userIdStr);
                session.close();
            }
        }
    }

    /**
     * 收到消息后
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("收到 WebSocket 消息：{}", message.getPayload());
        // 可以在这里处理客户端发送的消息
        super.handleTextMessage(session, message);
    }

    /**
     * 连接关闭后
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 从 sessions 中移除
        sessions.entrySet().removeIf(entry -> entry.getValue().equals(session));
        log.info("用户断开 WebSocket 连接，当前在线人数：{}", sessions.size());
    }

    /**
     * 发生错误时
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket 传输错误", exception);
        if (session.isOpen()) {
            session.close();
        }
    }

    /**
     * 向指定用户发送通知
     */
    public void sendNotification(Long userId, String message) {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
                log.info("已向用户 {} 发送通知：{}", userId, message);
            } catch (Exception e) {
                log.error("发送 WebSocket 消息失败，userId={}", userId, e);
            }
        } else {
            log.debug("用户 {} 不在线，无法发送 WebSocket 通知", userId);
        }
    }

    /**
     * 广播消息给所有在线用户
     */
    public void broadcast(String message) {
        for (Map.Entry<Long, WebSocketSession> entry : sessions.entrySet()) {
            try {
                entry.getValue().sendMessage(new TextMessage(message));
            } catch (Exception e) {
                log.error("广播消息失败，userId={}", entry.getKey(), e);
            }
        }
    }
}
