package com.itheima.knowledge_platform.enums;

public enum DocumentAuditStatusEnum {
    EDITING(0,"编辑中"),
    PENDING_REVIEW(1,"待审核"),
    APPROVED(2,"已通过"),
    REJECTED(3,"已驳回");

    private Integer code;
    private String message;

    DocumentAuditStatusEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    // 根据code获取枚举
    public static DocumentAuditStatusEnum getByCode(Integer code) {
        if (code == null) return null;
        for (DocumentAuditStatusEnum enums : DocumentAuditStatusEnum.values()) {
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
