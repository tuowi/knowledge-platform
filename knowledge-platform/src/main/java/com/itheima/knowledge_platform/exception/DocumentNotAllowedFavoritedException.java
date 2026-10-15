package com.itheima.knowledge_platform.exception;

/**
 * 文档不允许被收藏
 */
public class DocumentNotAllowedFavoritedException extends BaseException {
    public DocumentNotAllowedFavoritedException() {
    }

    public DocumentNotAllowedFavoritedException(String msg) {
        super(msg);
    }
}
