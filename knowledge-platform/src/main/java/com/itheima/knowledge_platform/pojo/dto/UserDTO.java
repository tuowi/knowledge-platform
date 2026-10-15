package com.itheima.knowledge_platform.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "用户注册的数据模型")
public class UserDTO implements Serializable {

    @Schema(description = "用户账号")
    private String username;

    @Schema(description = "用户密码")
    private String password;

    @Schema(description = "用户邮箱")
    private String email;

    @Schema(description = "头像URL")
    private String avatar;
}
