package com.pricepulse.backend.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {

    private Long id;
    private String username;
    private String role; // ADMIN 或 USER
    private LocalDateTime createdAt;
    private Integer status;

    // 不包含敏感信息如密码
}
