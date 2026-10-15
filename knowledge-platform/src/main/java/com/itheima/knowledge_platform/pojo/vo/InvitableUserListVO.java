package com.itheima.knowledge_platform.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InvitableUserListVO {
    private Long id;
    private String username;
    private String email;
}
