package com.itheima.knowledge_platform.service;

import com.itheima.knowledge_platform.pojo.vo.*;
import com.itheima.knowledge_platform.result.PageResult;

public interface TeamInviteApplyService {

    /**
     * 获取可邀请的用户列表
     *
     * @param id
     * @param username
     * @return
     */
    PageResult<InvitableUserListVO> invitableList(Integer page, Integer size, Long id, String username);

    /**
     * 发送入队邀请（队长）
     *
     * @param teamId
     * @param userId
     * @return
     */
    TeamInviteLeaderVO invitationsLeader(Long teamId, Long userId);

    /**
     * 获取我的入队邀请列表
     *
     * @param page
     * @param size
     * @param status
     * @return
     */
    PageResult<TeamInviteApplyPageVO> pageInvitations(Integer page, Integer size, Integer status);

    /**
     * 处理入队邀请（同意/拒绝）
     *
     * @param invitationId
     * @param status
     */
    void handleInvitations(Long invitationId, Integer status);

    /**
     * 主动申请加入团队
     *
     * @param id
     */
    void apply(Long id);

    /**
     * 获取我的申请列表
     *
     * @param page
     * @param size
     * @return
     */
    PageResult<TeamApplyPageVO> pageApply(Integer page, Integer size);

    /**
     * 处理入队申请（队长）
     *
     * @param applicationId
     * @param status
     */
    void applicationLeader(Long applicationId, Integer status);

    /**
     * 获取团队待处理的申请列表（队长）
     *
     * @param teamId
     * @param page
     * @param size
     * @return
     */
    PageResult<TeamApplyLeaderPageVO> applyLeaderPage(Long teamId, Integer page, Integer size);
}
