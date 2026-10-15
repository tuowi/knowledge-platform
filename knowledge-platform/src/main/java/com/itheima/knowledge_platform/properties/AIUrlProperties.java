package com.itheima.knowledge_platform.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ai.url")
@Data
public class AIUrlProperties {
    private String agentBaseUrl;
}
