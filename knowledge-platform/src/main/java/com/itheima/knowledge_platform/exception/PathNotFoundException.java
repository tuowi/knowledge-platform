package com.itheima.knowledge_platform.exception;

/**
 * 文档路径不存在
 */
public class PathNotFoundException extends BaseException {
    public PathNotFoundException() {
    }

    public PathNotFoundException(String msg) {
        super(msg);
    }
}
