package com.itheima.knowledge_platform.enums;

public enum TeamStatusEnum {
    PHILTRUM(0,"凑人中"),
    CREATED(1,"已创建"),
    DISBAND(2,"已解散");

    private Integer code;
    private String message;

    TeamStatusEnum(int code, String message) {
        this.code=code;
        this.message=message;
    }

    // 根据code获取枚举
    public static TeamStatusEnum getByCode(Integer code) {
        if (code == null) return null;
        for (TeamStatusEnum enums : TeamStatusEnum.values()) {
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
