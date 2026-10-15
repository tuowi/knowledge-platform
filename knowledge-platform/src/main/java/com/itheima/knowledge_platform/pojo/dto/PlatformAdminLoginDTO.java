package com.itheima.knowledge_platform.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "管理员登录时传递的数据模型")
public class PlatformAdminLoginDTO implements Serializable {

    @Schema(description = "管理员独立登录账号")
    private String adminAccount;

    @Schema(description = "管理员独立加密密码")
    private String adminPwd;
}
