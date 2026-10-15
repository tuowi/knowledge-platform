package com.itheima.knowledge_platform.exception;

/**
 * 文档找不到
 */
public class DocumentNotFoundException extends BaseException {
    public DocumentNotFoundException() {
    }

    public DocumentNotFoundException(String msg) {
        super(msg);
    }
}
