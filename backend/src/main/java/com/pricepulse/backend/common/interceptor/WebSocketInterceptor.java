package com.pricepulse.backend.common.interceptor;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;


import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@Component
@Slf4j
public class WebSocketInterceptor implements HandshakeInterceptor {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest serverRequest = (ServletServerHttpRequest) request;
            HttpServletRequest servletRequest = serverRequest.getServletRequest();

            // 获取 Token
            String token = servletRequest.getParameter("token");

            if (token == null || token.isEmpty()) {
                log.warn("WebSocket 握手失败：缺少 token");
                return false;
            }

            try {
                // 验证 Token
                Claims claims = Jwts.parser()
                        .setSigningKey(jwtSecret.getBytes())
                        .parseClaimsJws(token)
                        .getBody();

                Long userId = claims.get("userId", Long.class);

                if (userId != null) {
                    // 将 userId 放入 attributes，供 Handler 使用
                    attributes.put("userId", userId.toString());
                    log.info("WebSocket 握手成功，userId={}", userId);
                    return true;
                } else {
                    log.warn("Token 中缺少 userId");
                    return false;
                }
            } catch (Exception e) {
                log.error("Token 验证失败：{}", e.getMessage());
                return false;
            }
        }

        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 握手后处理，通常不需要做什么
    }
}
