package com.itheima.knowledge_platform.controller.admin;

import com.itheima.knowledge_platform.pojo.dto.UserQueryRequestDTO;
import com.itheima.knowledge_platform.pojo.vo.UserAdminVO;
import com.itheima.knowledge_platform.pojo.vo.UserDetailVO;
import com.itheima.knowledge_platform.pojo.vo.UserStatisticsOverviewVO;
import com.itheima.knowledge_platform.result.PageResult;
import com.itheima.knowledge_platform.result.Result;
import com.itheima.knowledge_platform.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "用户管控模块")
@RequestMapping("/admin/users")
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取所有用户列表
     */
    @Operation(summary = "获取所有用户列表")
    @GetMapping
    public Result<PageResult<UserAdminVO>> userManagePage(@Valid UserQueryRequestDTO userQueryRequestDTO) {
        log.info("获取所有用户列表：{}", userQueryRequestDTO);
        PageResult<UserAdminVO> result = userService.adminPage(userQueryRequestDTO);
        return Result.success(result);
    }

    /**
     * 禁用/启用用户
     */
    @Operation(summary = "禁用/启用用户")
    @PutMapping("/{id}/status")
    public Result<Void> setUserStatus(@PathVariable Long id, Integer status) {
        log.info("禁用/启用用户,用户id：{}，设置状态：{}", id, status);
        userService.setUserStatus(id, status);
        return Result.success();
    }

    /**
     * 获取用户详情
     */
    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    public Result<UserDetailVO> userDetail(@PathVariable Long id) {
        log.info("获取用户详情:{}", id);
        UserDetailVO userDetailVO = userService.userDetail(id);
        return Result.success(userDetailVO);
    }

    /**
     * 获取用户文档统计
     */
    @Operation(summary = "获取用户文档统计")
    @GetMapping("/{id}/statistics")
    public Result<UserStatisticsOverviewVO> statisticsUser(@PathVariable Long id){
        log.info("获取用户文档统计:{}",id);
        UserStatisticsOverviewVO userStatisticsOverviewVO=userService.statisticsUser(id);
        return Result.success(userStatisticsOverviewVO);
    }
}
