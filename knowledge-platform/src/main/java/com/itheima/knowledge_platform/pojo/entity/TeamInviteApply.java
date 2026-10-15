package com.itheima.knowledge_platform.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 团队邀请&申请表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamInviteApply implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long teamId;
    private Long applyUserId;
    private Long operateUserId;
    private Integer type; // 0=队长邀请用户，1=用户主动申请
    private Integer status; // 0=待处理，1=同意，2=拒绝，3=已过期

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime handleAt;
}
