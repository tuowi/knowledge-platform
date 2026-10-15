package com.itheima.knowledge_platform.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "page.team-member")
@Data
public class PageTeamMemberProperties {
    //页码
    private Integer pageNum;

    //单页大小
    private Integer pageSize;
}
