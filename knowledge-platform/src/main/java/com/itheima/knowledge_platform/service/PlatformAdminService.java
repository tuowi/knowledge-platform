package com.itheima.knowledge_platform.service;

import com.itheima.knowledge_platform.pojo.dto.PlatformAdminAddDTO;
import com.itheima.knowledge_platform.pojo.dto.PlatformAdminLoginDTO;
import com.itheima.knowledge_platform.pojo.entity.PlatformAdmin;
import com.itheima.knowledge_platform.pojo.vo.PlatformAdminLoginVO;
import com.itheima.knowledge_platform.result.PageResult;

public interface PlatformAdminService {

    /**
     * 管理员登录
     *
     * @param platformAdminLoginDTO
     * @return
     */
    PlatformAdminLoginVO login(PlatformAdminLoginDTO platformAdminLoginDTO);

    /**
     * 新增子管理员
     *
     * @param platformAdminAddDTO
     */
    PlatformAdmin add(PlatformAdminAddDTO platformAdminAddDTO);

    /**
     * 启用/禁用子管理员
     *
     * @param id
     */
    void setStatus(Long id, Integer status);

    /**
     * 删除子管理员
     * @param id
     */
    void delete(Long id);

    /**
     * 重置子管理员密码
     * @param id
     */
    void updatePassword(Long id);

    /**
     * 获取子管理员列表
     * @param page
     * @param size
     * @param keyword
     * @return
     */
    PageResult<PlatformAdmin> page(Integer page, Integer size, String keyword);
}
