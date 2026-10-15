package com.itheima.knowledge_platform.enums;

// 用户身份枚举
public enum IdentityEnum {
    NORMAL(0, "普通用户"),
    TEAM_LEADER(1, "团队队长");

    private Integer code;
    private String message;

    IdentityEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    // 根据code获取枚举
    public static IdentityEnum getByCode(Integer code) {
        if (code == null) return null;
        for (IdentityEnum enums : IdentityEnum.values()) {
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
