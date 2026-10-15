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
@Schema(description = "管理员成功登录返回的数据格式")
public class PlatformAdminLoginVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "管理员账号")
    private String adminAccount;

    @Schema(description = "管理员类型：0=子管理员，1=超级管理员")
    private Integer adminType;

    @Schema(description = "角色名称（前端展示用）")
    private String roleName;

    @Schema(description = "jwt令牌")
    private String token;
}
