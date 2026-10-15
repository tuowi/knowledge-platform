package com.itheima.knowledge_platform.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeamInviteMemberDetailVO {
    private Long id;
    private Long userId;
    private String username;
    private Integer status;
    private String statusName;
}
