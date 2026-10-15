package com.itheima.knowledge_platform.pojo.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class CommentDTO implements Serializable {
    private Long documentId;
    private Long parentId; // 父评论ID
    private String content;

    private List<Long> mentionedUsers;
}
