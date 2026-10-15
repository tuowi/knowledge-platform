package com.itheima.knowledge_platform.exception;

/**
 * 评论出现错误
 */
public class CommentErrorException extends BaseException{
    public CommentErrorException() {
    }

    public CommentErrorException(String msg) {
        super(msg);
    }
}
