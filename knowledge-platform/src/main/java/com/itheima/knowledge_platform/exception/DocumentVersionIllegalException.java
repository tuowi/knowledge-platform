package com.itheima.knowledge_platform.exception;

/**
 * 版本号不合法
 */
public class DocumentVersionIllegalException extends BaseException {
    public DocumentVersionIllegalException() {
    }

    public DocumentVersionIllegalException(String msg) {
        super(msg);
    }
}
