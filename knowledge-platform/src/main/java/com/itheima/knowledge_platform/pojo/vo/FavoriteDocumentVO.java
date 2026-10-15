package com.itheima.knowledge_platform.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FavoriteDocumentVO {
    // 收藏记录字段
    private Long id;
    private Long documentId;

    // 文档元数据字段
    private String title;
    private Integer visibility;
    private Long teamId;
    private Long authorId;
    private Integer status;
    private String authorName;
    private Integer viewCount;
    private Integer likeCount;
    private Integer favoriteCount;

    //何时收藏
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;
}
