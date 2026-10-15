package com.itheima.knowledge_platform.exception;

/**
 * 文档不允许修改
 */
public class DocumentLockedException extends BaseException {
    public DocumentLockedException() {
    }

    public DocumentLockedException(String msg) {
        super(msg);
    }
}
