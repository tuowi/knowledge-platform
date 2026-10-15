package com.itheima.knowledge_platform.enums;

// 审核状态枚举
public enum AuditStatusEnum {
    EDITING(0,"编辑中"),
    PENDING(1, "待审核"),
    APPROVED(2, "已通过"),
    REJECTED(3, "已驳回");

    private Integer code;
    private String message;

    AuditStatusEnum(int code, String message) {
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
