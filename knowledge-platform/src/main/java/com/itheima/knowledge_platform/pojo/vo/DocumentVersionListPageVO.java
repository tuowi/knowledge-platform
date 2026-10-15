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
@Schema(description = "获取文档历史版本列表数据模型")
public class DocumentVersionListPageVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "版本号")
    private Integer version;

    @Schema(description = "编辑人")
    private Long editorId;

    @Schema(description = "编辑人账号名")
    private String editorName;

    @Schema(description = "修改评论")
    private String changeRemark;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;
}
