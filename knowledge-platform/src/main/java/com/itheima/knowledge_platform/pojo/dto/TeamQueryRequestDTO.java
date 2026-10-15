package com.itheima.knowledge_platform.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "团队查询请求参数")
public class TeamQueryRequestDTO implements Serializable {

    @Schema(description = "页码", example = "1", defaultValue = "1")
    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    @Schema(description = "每页大小", example = "10", defaultValue = "10")
    @Min(value = 1, message = "每页大小最小为1")
    @Max(value = 100, message = "每页大小最大为100")
    private Integer size = 10;

    @Schema(description = "搜索关键词（匹配团队名称）", example = "技术研发部")
    private String keyword;

    @Schema(description = "团队状态：0=待创建，1=已创建（正常），2=已解散", example = "1", allowableValues = {"0", "1", "2"})
    @Min(value = 0, message = "状态值最小为0")
    @Max(value = 2, message = "状态值最大为2")
    private Integer status;
}
