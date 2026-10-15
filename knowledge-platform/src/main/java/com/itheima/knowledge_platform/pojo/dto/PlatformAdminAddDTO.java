package com.itheima.knowledge_platform.pojo.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PlatformAdminAddDTO implements Serializable {
    private String adminAccount;
    private String adminPwd;
    private Integer adminType;
}
