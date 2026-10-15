package com.itheima.knowledge_platform.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "评论响应数据")
public class CommentAdminVO implements Serializable {

    @Schema(description = "评论ID", example = "1")
    private Long id;

    @Schema(description = "文档ID", example = "123")
    private Long documentId;

    @Schema(description = "文档标题", example = "Spring Boot入门教程")
    private String documentTitle;

    @Schema(description = "父评论ID（顶级评论为null）", example = "null")
    private Long parentId;

    @Schema(description = "父评论内容", example = "null")
    private String parentContent;

    @Schema(description = "用户ID", example = "5")
    private Long userId;

    @Schema(description = "用户名", example = "张三")
    private String userName;

    @Schema(description = "用户头像URL", example = "http://...")
    private String userAvatar;

    @Schema(description = "评论内容", example = "写得很棒！")
    private String content;

    @Schema(description = "回复数量", example = "3")
    private Integer replyCount;

    @Schema(description = "创建时间", example = "2024-01-20 15:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;
}
