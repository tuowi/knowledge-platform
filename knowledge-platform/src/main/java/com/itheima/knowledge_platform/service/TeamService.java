package com.itheima.knowledge_platform.service;

import com.itheima.knowledge_platform.pojo.dto.TeamQueryRequestDTO;
import com.itheima.knowledge_platform.pojo.entity.TeamInviteApply;
import com.itheima.knowledge_platform.pojo.vo.*;
import com.itheima.knowledge_platform.result.PageResult;
import jakarta.validation.Valid;

import java.util.List;

public interface TeamService {

    /**
     * 创建团队
     * @param name
     * @param inviteUserIds
     * @return
     */
    TeamVO create(String name, List<Long> inviteUserIds);

    /**
     * 获取团队列表
     *
     * @param page
     * @param size
     * @param status
     * @return
     */
    PageResult<TeamPageVO> page(Integer page, Integer size, Integer status);

    /**
     * 获取团队详情
     * @param id
     * @return
     */
    TeamDetailVO teamDetail(Long id);

    /**
     * 获取所有团队列表
     * @param teamQueryRequestDTO
     * @return
     */
    PageResult<TeamAdminVO> pageAdmin(@Valid TeamQueryRequestDTO teamQueryRequestDTO);

    /**
     * 获取团队详情
     * @param id
     * @return
     */
    TeamAdminDetailVO teamDetailAdmin(Long id);

    /**
     * 获取可申请团队列表
     * @param page
     * @param size
     * @param teamName
     * @return
     */
    PageResult<TeamAvailableApplyVO> availableApply(Integer page, Integer size, String teamName);
}
