package com.itheima.knowledge_platform.service;

import com.itheima.knowledge_platform.pojo.vo.TeamMemberDetailPageVO;
import com.itheima.knowledge_platform.result.PageResult;

public interface TeamMemberService {

    /**
     * 获取团队成员列表
     * @param teamId
     * @param page
     * @param size
     * @return
     */
    PageResult<TeamMemberDetailPageVO> memberPage(Long teamId, Integer page, Integer size);

    /**
     *  移出团队成员（队长）
     * @param teamId
     * @param userId
     */
    void deleteMemberByUserId(Long teamId, Long userId);

    /**
     * 退出团队
     * @param teamId
     */
    void exit(Long teamId);

    /**
     * 解散团队（队长）
     * @param teamId
     */
    void disband(Long teamId);

    /**
     * 强制解散团队
     * @param id
     */
    void disbandAdmin(Long id);
}
