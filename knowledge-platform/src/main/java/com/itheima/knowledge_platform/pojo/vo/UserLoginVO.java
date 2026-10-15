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
@Schema(description = "用户成功登录返回的数据格式")
public class UserLoginVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "用户账号")
    private String username;

    @Schema(description = "jwt令牌")
    private String token;
}
