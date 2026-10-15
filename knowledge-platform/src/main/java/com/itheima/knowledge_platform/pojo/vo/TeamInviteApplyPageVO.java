package com.itheima.knowledge_platform.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamInviteApplyPageVO {
    private Long id;
    private Long teamId;
    private String teamName;//队名
    private Long applyUserId;
    private String applyUserName;//邀请人账户名
    private Integer status; // 0=待处理，1=同意，2=拒绝，3=已过期

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;
}
