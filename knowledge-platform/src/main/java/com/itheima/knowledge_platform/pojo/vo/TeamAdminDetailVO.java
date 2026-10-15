package com.itheima.knowledge_platform.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "团队详细信息响应数据")
public class TeamAdminDetailVO implements Serializable {

    @Schema(description = "团队ID", example = "1")
    private Long id;

    @Schema(description = "团队名称", example = "技术研发部")
    private String name;

    @Schema(description = "队长用户ID", example = "2")
    private Long ownerUserId;

    @Schema(description = "队长用户名", example = "李四")
    private String ownerName;

    @Schema(description = "队长邮箱", example = "lisi@example.com")
    private String ownerEmail;

    @Schema(description = "成员数量", example = "8")
    private Integer memberCount;

    @Schema(description = "文档数量", example = "45")
    private Integer documentCount;

    @Schema(description = "团队状态：0=待创建，1=已创建（正常），2=已解散", example = "1")
    private Integer status;

    @Schema(description = "创建时间", example = "2024-01-01 00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;

    @Schema(description = "成员列表")
    private List<TeamMember> members;

    @Data
    @Schema(description = "团队成员信息")
    public static class TeamMember implements Serializable {
        @Schema(description = "用户ID", example = "2")
        private Long userId;

        @Schema(description = "用户名", example = "李四")
        private String username;

        @Schema(description = "角色：队长/成员", example = "队长")
        private String role;
    }
}
