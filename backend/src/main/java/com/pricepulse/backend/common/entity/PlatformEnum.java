package com.pricepulse.backend.common.entity;

/**
 * 电商平台枚举
 */
public enum PlatformEnum {
    TAOBAO("淘宝"),
    TMALL("天猫"),
    JD("京东"),
    PDD("拼多多"),
    OTHER("其他");

    private final String description;

    PlatformEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static PlatformEnum fromString(String platform) {
        for (PlatformEnum p : PlatformEnum.values()) {
            if (p.name().equalsIgnoreCase(platform)) {
                return p;
            }
        }
        return OTHER;
    }
}
