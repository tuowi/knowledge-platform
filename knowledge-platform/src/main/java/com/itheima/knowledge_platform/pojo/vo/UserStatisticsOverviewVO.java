package com.itheima.knowledge_platform.pojo.vo;

import com.itheima.knowledge_platform.pojo.inner.document.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "用户统计概览响应数据")
public class UserStatisticsOverviewVO implements Serializable {

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    @Schema(description = "汇总统计")
    private Summary summary;

    @Schema(description = "按文档状态统计")
    private ByStatus byStatus;

    @Schema(description = "按审核状态统计")
    private ByAuditStatus byAuditStatus;

    @Schema(description = "按可见范围统计")
    private ByVisibility byVisibility;
}
