package com.itheima.knowledge_platform.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamMemberDetailVO {
    private Long id;
    private Long userId;
    private String username;
    private Integer teamRole;
    private String role;
}
