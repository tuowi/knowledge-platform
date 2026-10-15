package com.itheima.knowledge_platform.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户登录时传递的数据模型")
public class UserLoginDTO {

    @Schema(description = "用户独立登录账号")
    private String username;

    @Schema(description = "用户独立加密密码")
    private String password;
}
