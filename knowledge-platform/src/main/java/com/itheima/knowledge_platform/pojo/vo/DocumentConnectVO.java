package com.itheima.knowledge_platform.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "内容关联成功返回的数据模型")
public class DocumentConnectVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "内容地址")
    private String content;

    @Schema(description = "版本号")
    private Integer version;
}
