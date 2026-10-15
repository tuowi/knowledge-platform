package com.itheima.knowledge_platform.pojo.dto;

import jakarta.validation.constraints.Max;
import lombok.Data;
import jakarta.validation.constraints.Min;

import java.io.Serializable;

@Data
public class DocumentAdminDetailPageDTO implements Serializable {

    /**
     * 页码，默认1
     */
    private Integer page = 1;

    /**
     * 每页大小，默认10
     */
    private Integer size = 10;

    /**
     * 文档标题模糊搜索
     */
    private String title;

    /**
     * 作者名称模糊搜索
     */
    private String authorName;

    /**
     * 按团队筛选
     */
    private Long teamId;

    /**
     * 审核状态：1=待审核，2=已通过，3=已驳回
     */
    @Min(value = 1, message = "审核状态最小值为1")
    @Max(value = 3, message = "审核状态最大值是3")
    private Integer auditStatus;

    /**
     * 文档状态：1=已发布，2=已归档
     */
    @Min(value = 1, message = "文档状态最小值为1")
    @Max(value = 2, message = "文档状态最大值是2")
    private Integer status;

    /**
     * 创建开始时间
     */
    private String startTime;

    /**
     * 创建结束时间
     */
    private String endTime;
}
