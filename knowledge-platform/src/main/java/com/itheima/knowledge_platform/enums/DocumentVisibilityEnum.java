package com.itheima.knowledge_platform.enums;

//可见范围
public enum DocumentVisibilityEnum {
    PRIVATE(0,"私有"),
    WITHIN_THE_TEAM(1,"团队内"),
    PUBLIC(2,"公开");

    private Integer code;
    private String message;

    DocumentVisibilityEnum(int code, String message) {
        this.code=code;
        this.message=message;
    }

    // 根据code获取枚举
    public static DocumentVisibilityEnum getByCode(Integer code) {
        if (code == null) return null;
        for (DocumentVisibilityEnum enums : DocumentVisibilityEnum.values()) {
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
