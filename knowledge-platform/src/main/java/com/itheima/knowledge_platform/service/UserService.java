package com.itheima.knowledge_platform.service;

import com.itheima.knowledge_platform.pojo.dto.UserDTO;
import com.itheima.knowledge_platform.pojo.dto.UserLoginDTO;
import com.itheima.knowledge_platform.pojo.dto.UserQueryRequestDTO;
import com.itheima.knowledge_platform.pojo.vo.UserAdminVO;
import com.itheima.knowledge_platform.pojo.vo.UserDetailVO;
import com.itheima.knowledge_platform.pojo.vo.UserLoginVO;
import com.itheima.knowledge_platform.pojo.vo.UserStatisticsOverviewVO;
import com.itheima.knowledge_platform.result.PageResult;

public interface UserService {

    /**
     * 用户登录
     * @param userLoginDTO
     * @return
     */
    UserLoginVO login(UserLoginDTO userLoginDTO);

    /**
     * 用户注册
     * @param userDTO
     */
    void register(UserDTO userDTO);

    /**
     * 用户更改信息
     * @param userDTO
     */
    void modify(UserDTO userDTO);

    /**
     * 修改密码
     * @param oldPassword
     * @param newPassword
     */
    void editPassword(String oldPassword, String newPassword);

    /**
     * 获取用户列表
     * @param userQueryRequestDTO
     * @return
     */
    PageResult<UserAdminVO> adminPage(UserQueryRequestDTO userQueryRequestDTO);

    /**
     * 禁用/启用用户
     * @param id
     * @param status
     */
    void setUserStatus(Long id, Integer status);

    /**
     * 获取用户详情
     * @param id
     * @return
     */
    UserDetailVO userDetail(Long id);

    /**
     * 获取用户文档统计
     * @param id
     * @return
     */
    UserStatisticsOverviewVO statisticsUser(Long id);
}
