package com.itheima.knowledge_platform.enums;

public enum PlatformAdminTypeEnum {
    SUB_ADMIN(0, "子管理员"),
    SUPER_ADMIN(1, "超级管理员");

    private Integer code;
    private String message;

    PlatformAdminTypeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    // 根据code获取枚举
    public static PlatformAdminTypeEnum getByCode(Integer code) {
        if (code == null) return null;
        for (PlatformAdminTypeEnum enums : PlatformAdminTypeEnum.values()) {
            if (enums.getCode().equals(code)) {
                return enums;
            }
        }
        return null;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
