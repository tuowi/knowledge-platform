package com.itheima.knowledge_platform.enums;

public enum TeamInviteApplyTypeEnum {
    CAPTAIN_INVITE_USER(0,"队长邀请用户"),
    USER_APPLY_TEAM(1,"用户主动申请");

    private Integer code;
    private String message;

    TeamInviteApplyTypeEnum(int code, String message) {
        this.code=code;
        this.message=message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
