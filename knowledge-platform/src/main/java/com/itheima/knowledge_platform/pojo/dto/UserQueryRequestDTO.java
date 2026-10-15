package com.itheima.knowledge_platform.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "用户查询请求参数")
public class UserQueryRequestDTO implements Serializable {

    @Schema(description = "页码", example = "1", defaultValue = "1")
    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    @Schema(description = "每页大小", example = "10", defaultValue = "10")
    @Min(value = 1, message = "每页大小最小为1")
    @Max(value = 100, message = "每页大小最大为100")
    private Integer size = 10;

    @Schema(description = "搜索关键词（模糊匹配用户名、邮箱）", example = "张三")
    private String username;

    @Schema(description = "账号状态筛选：0=禁用，1=启用，不传则查全部", example = "1", allowableValues = {"0", "1"})
    @Min(value = 0, message = "状态值最小为0")
    @Max(value = 1, message = "状态值最大为1")
    private Integer status;
}
