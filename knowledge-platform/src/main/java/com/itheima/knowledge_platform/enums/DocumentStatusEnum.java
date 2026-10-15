package com.itheima.knowledge_platform.enums;

import lombok.AllArgsConstructor;
import lombok.Data;

// 文档状态枚举
public enum DocumentStatusEnum {
    DRAFT(0, "草稿"),
    PUBLISHED(1, "已发布"),
    ARCHIVED(2, "已归档");

    private Integer code;
    private String message;

    DocumentStatusEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    // 根据code获取枚举
    public static DocumentStatusEnum getByCode(Integer code) {
        if (code == null) return null;
        for (DocumentStatusEnum enums : DocumentStatusEnum.values()) {
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