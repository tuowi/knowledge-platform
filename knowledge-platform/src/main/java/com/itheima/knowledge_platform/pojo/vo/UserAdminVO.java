package com.itheima.knowledge_platform.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "用户响应数据")
public class UserAdminVO implements Serializable {

    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "用户账号", example = "zhangsan")
    private String username;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    @Schema(description = "头像URL", example = "https://xxx.com/avatar.jpg")
    private String avatar;

    @Schema(description = "账号状态：0=禁用，1=启用", example = "1")
    private Integer status;

    @Schema(description = "文档数量", example = "25")
    private Integer documentCount;

    @Schema(description = "团队数量", example = "2")
    private Integer teamCount;

    @Schema(description = "团队名称列表", example = "[\"技术研发部\", \"产品部\"]")
    private List<String> teamNames;

    @Schema(description = "创建时间", example = "2024-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;
}