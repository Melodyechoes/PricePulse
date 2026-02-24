package com.pricepulse.backend.common.entity;

import lombok.Data;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
}
