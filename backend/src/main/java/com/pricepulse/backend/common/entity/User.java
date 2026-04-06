package com.pricepulse.backend.common.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String role; // ADMIN 或 USER
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer status;
}
