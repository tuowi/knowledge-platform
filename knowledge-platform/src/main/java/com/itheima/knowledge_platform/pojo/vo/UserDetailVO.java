package com.itheima.knowledge_platform.pojo.vo;

import com.itheima.knowledge_platform.pojo.inner.user.UserStatistics;
import com.itheima.knowledge_platform.pojo.inner.user.UserTeam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "用户详细信息响应数据")
public class UserDetailVO implements Serializable {

    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    @Schema(description = "头像URL", example = "https://xxx.com/avatar.jpg")
    private String avatar;

    @Schema(description = "账号状态：0=禁用，1=启用", example = "1")
    private Integer status;

    @Schema(description = "账号状态描述", example = "启用")
    private String statusDesc;

    @Schema(description = "创建时间", example = "2024-01-01 10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "统计数据")
    private UserStatistics statistics;

    @Schema(description = "团队列表")
    private List<UserTeam> teams;

//    @Schema(description = "最近活动列表")
//    private List<RecentActivity> recentActivities;

    //可选-----需要操作日志
/*    @Data
    @Schema(description = "最近活动")
    public static class RecentActivity implements Serializable {
        @Schema(description = "操作类型", example = "发布文档")
        private String action;

        @Schema(description = "操作目标", example = "Spring Boot教程")
        private String target;

        @Schema(description = "操作时间", example = "2024-05-14 10:00:00")
        private LocalDateTime createdAt;
    }*/
}