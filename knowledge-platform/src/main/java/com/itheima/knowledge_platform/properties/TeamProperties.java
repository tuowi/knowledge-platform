package com.itheima.knowledge_platform.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "team.user")
@Data
public class TeamProperties {

    //一个用户最多创建团队数目
    private Integer maxCreatedTeam;

    //一个团队最少人数
    private Integer minTeamMember;

    //一个团队最多人数
    private Integer maxTeamMember;

    //推迟天数
    private Integer expired;
}
