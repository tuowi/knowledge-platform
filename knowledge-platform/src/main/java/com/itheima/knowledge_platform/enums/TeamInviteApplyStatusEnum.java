package com.itheima.knowledge_platform.enums;

public enum TeamInviteApplyStatusEnum {
    PENDING(0, "待处理"),
    AGREE(1, "同意"),
    REJECT(2, "拒绝"),
    EXPIRED(3, "已过期");

    private Integer code;
    private String message;

    TeamInviteApplyStatusEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    // 根据code获取枚举
    public static TeamInviteApplyStatusEnum getByCode(Integer code) {
        if (code == null) return null;
        for (TeamInviteApplyStatusEnum enums : TeamInviteApplyStatusEnum.values()) {
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
