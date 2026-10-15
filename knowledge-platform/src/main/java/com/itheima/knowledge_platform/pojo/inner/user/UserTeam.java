package com.itheima.knowledge_platform.pojo.inner.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "用户团队信息")
public class UserTeam implements Serializable {
    @Schema(description = "团队ID", example = "1")
    private Long id;

    @Schema(description = "团队名称", example = "技术研发部")
    private String name;

    @Schema(description = "角色：OWNER=所有者，MEMBER=成员", example = "OWNER")
    private String role;

    @Schema(description = "加入时间", example = "2024-01-15 09:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime joinedAt;
}