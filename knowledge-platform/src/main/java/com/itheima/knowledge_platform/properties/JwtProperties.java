package com.itheima.knowledge_platform.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
//自动把配置文件里以 knowledge-platform.jwt 开头的配置，注入到当前类的字段里
@ConfigurationProperties(prefix = "knowledge-platform.jwt")
@Data
public class JwtProperties {

    /**
     * 管理端员工生成jwt令牌相关配置
     */
    private String adminSecretKey;//自动注入：code
    private long adminTtl;// 会自动注入：7200000
    private String adminTokenName;//token

    /**
     * 用户端微信用户生成jwt令牌相关配置
     */
    private String userSecretKey;
    private long userTtl;
    private String userTokenName;

}
