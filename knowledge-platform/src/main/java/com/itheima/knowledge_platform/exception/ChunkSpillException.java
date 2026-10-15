package com.itheima.knowledge_platform.exception;

/**
 * 分块过大溢出
 */
public class ChunkSpillException extends BaseException {
    public ChunkSpillException() {
    }

    public ChunkSpillException(String msg) {
        super(msg);
    }
}
