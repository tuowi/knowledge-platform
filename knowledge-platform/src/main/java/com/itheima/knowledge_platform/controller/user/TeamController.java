package com.itheima.knowledge_platform.controller.user;

import com.itheima.knowledge_platform.pojo.vo.*;
import com.itheima.knowledge_platform.properties.PageTeamMemberProperties;
import com.itheima.knowledge_platform.result.PageResult;
import com.itheima.knowledge_platform.result.Result;
import com.itheima.knowledge_platform.service.TeamInviteApplyService;
import com.itheima.knowledge_platform.service.TeamMemberService;
import com.itheima.knowledge_platform.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Tag(name = "团队管理模块")
@RequestMapping("/user/teams")
@RestController("userTeamController")
public class TeamController {

    @Autowired
    private TeamService teamService;

    @Autowired
    private TeamInviteApplyService teamInviteApplyService;

    @Autowired
    private TeamMemberService teamMemberService;

    /**
     * 创建团队
     */
    @Operation(summary = "创建团队")
    @PostMapping
    public Result<TeamVO> create(@RequestParam String name, @RequestParam(required = false) ArrayList<Long> inviteUserIds) {
        log.info("创建团队：{},邀请的成员：{}", name, inviteUserIds);
        TeamVO teamVO = teamService.create(name, inviteUserIds);
        return Result.success(teamVO);
    }

    /**
     * 获取团队列表
     */
    @Operation(summary = "获取团队列表")
    @GetMapping
    public Result<PageResult<TeamPageVO>> page(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "status", required = false) Integer status) {
        log.info("获取团队列表:{},{},{}", page, size, status);
        PageResult<TeamPageVO> teamVOList = teamService.page(page, size, status);
        return Result.success(teamVOList);
    }

    /**
     * 获取团队详情
     * 校验用户是否有权限查看该团队（必须是团队成员或队长）
     * 查询团队基本信息
     * 查询成员列表（前5个或分页）
     * 查询邀请记录状态（队长可见）
     * 返回详细信息
     */
    @Operation(summary = "获取团队详情")
    @GetMapping("/{id}")
    public Result<TeamDetailVO> teamDetail(@PathVariable Long id) {
        log.info("获取团队详情:{}", id);
        TeamDetailVO teamDetailVO = teamService.teamDetail(id);
        return Result.success(teamDetailVO);
    }

    /**
     * 获取可邀请的用户列表
     * 功能：获取可以邀请加入团队的用户列表（排除已在团队中的和已邀请过的）
     * 输入：团队ID、关键词搜索（可选）
     * 输出：可邀请的用户列表
     */
    @Operation(summary = "获取可邀请的用户列表（队长）")
    @GetMapping("/{id}/invitable-users")
    public Result<PageResult<InvitableUserListVO>> invitableList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @PathVariable Long id,
            @RequestParam(value = "username", required = false)
            String username
    ) {
        log.info("获取可邀请的用户列表:{}", id);
        PageResult<InvitableUserListVO> voList = teamInviteApplyService.invitableList(page, size, id, username);
        return Result.success(voList);
    }

    /**
     * 发送入队邀请（队长）
     * 功能：队长邀请指定用户加入团队
     * 输入：团队ID、被邀请用户ID
     * 输出：邀请记录信息
     */
    @Operation(summary = " 发送入队邀请（队长）")
    @PostMapping("/{id}/invitations")
    public Result<TeamInviteLeaderVO> invitationsLeader(@PathVariable Long id, Long userId) {
        log.info("发送入队邀请（队长）:{},{}", id, userId);
        TeamInviteLeaderVO teamInviteLeaderVO = teamInviteApplyService.invitationsLeader(id, userId);
        return Result.success(teamInviteLeaderVO);
    }

    /**
     * 获取我的入队邀请列表
     */
    @Operation(summary = "获取我的入队邀请列表")
    @GetMapping("/invitations")
    public Result<PageResult<TeamInviteApplyPageVO>> pageInvitations(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "status", required = false) Integer status) {
        log.info("获取我的入队邀请列表");
        PageResult<TeamInviteApplyPageVO> result = teamInviteApplyService.pageInvitations(page, size, status);
        return Result.success(result);
    }

    /**
     * 处理入队邀请（同意/拒绝）
     */
    @Operation(summary = "处理入队邀请（同意/拒绝）")
    @PutMapping("/invitations/{invitationId}")
    public Result<Void> handleInvitations(@PathVariable Long invitationId, Integer status) {
        log.info("处理入队邀请（同意/拒绝）:{}", invitationId);
        teamInviteApplyService.handleInvitations(invitationId, status);
        return Result.success();
    }

    /**
     * 主动申请加入团队
     */
    @Operation(summary = "主动申请加入团队")
    @PostMapping("/{id}/apply")
    public Result<Void> apply(@PathVariable Long id) {
        log.info("主动申请加入团队:{}", id);
        teamInviteApplyService.apply(id);
        return Result.success();
    }

    /**
     * 获取我的申请列表
     */
    @Operation(summary = "获取我的申请列表")
    @GetMapping("/my-applications")
    public Result<PageResult<TeamApplyPageVO>> pageApply(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size
    ) {
        log.info("获取我的申请列表");
        PageResult<TeamApplyPageVO> applyPageVOPageResult = teamInviteApplyService.pageApply(page, size);
        return Result.success(applyPageVOPageResult);
    }

    /**
     * 处理入队申请（队长）
     */
    @Operation(summary = "处理入队申请（队长）")
    @PutMapping("/applications/{applicationId}")
    public Result<Void> applicationLeader(@PathVariable Long applicationId, Integer status) {
        log.info("处理入队申请（队长）:{}", applicationId);
        teamInviteApplyService.applicationLeader(applicationId, status);
        return Result.success();
    }

    /**
     * 获取团队待处理的申请列表（队长）
     */
    @Operation(summary = "获取团队待处理的申请列表（队长）")
    @GetMapping("/{id}/applications/pending")
    public Result<PageResult<TeamApplyLeaderPageVO>> applyLeaderPage(
            @PathVariable Long id,
            @RequestParam(value = "page", defaultValue = "1", required = false) Integer page,
            @RequestParam(value = "size", defaultValue = "10", required = false) Integer size
    ) {
        log.info("获取团队待处理的申请列表（队长）:{}", id);
        PageResult<TeamApplyLeaderPageVO> pageResult = teamInviteApplyService.applyLeaderPage(id, page, size);
        return Result.success(pageResult);
    }

    /**
     * 获取团队成员列表
     */
    @Operation(summary = "获取团队成员列表")
    @GetMapping("/{id}/members")
    public Result<PageResult<TeamMemberDetailPageVO>> memberPage(
            @PathVariable Long id,
            @RequestParam(value = "page", defaultValue = "1", required = false) Integer page,
            @RequestParam(value = "size", defaultValue = "10", required = false) Integer size
    ) {
        log.info("获取团队成员列表:{}", id);
        PageResult<TeamMemberDetailPageVO> result = teamMemberService.memberPage(id, page, size);
        return Result.success(result);
    }

    /**
     * 移出团队成员（队长）
     */
    @Operation(summary = "移出团队成员（队长）")
    @DeleteMapping("/{id}/members/{userId}")
    public Result<Void> deleteMember(@PathVariable Long id, @PathVariable Long userId) {
        log.info("移出团队成员（队长）  团队ID：{}，移出用户ID：{}", id, userId);
        teamMemberService.deleteMemberByUserId(id, userId);
        return Result.success();
    }

    /**
     * 退出团队
     */
    @Operation(summary = "退出团队")
    @PostMapping("/{id}/quit")
    public Result<Void> exit(@PathVariable Long id) {
        log.info("退出团队:{}", id);
        teamMemberService.exit(id);
        return Result.success();
    }

    /**
     * 解散团队（队长）
     */
    @Operation(summary = "解散团队（队长）")
    @DeleteMapping("/{id}")
    public Result<Void> disband(@PathVariable Long id) {
        log.info("解散团队（队长）:{}", id);
        teamMemberService.disband(id);
        return Result.success();
    }

    /**
     * 获取可申请团队列表
     */
    @Operation(summary = "获取可申请团队列表")
    @GetMapping("/available")
    public Result<PageResult<TeamAvailableApplyVO>> available(
            @RequestParam(value = "page", defaultValue = "1", required = false) Integer page,
            @RequestParam(value = "size", defaultValue = "10", required = false) Integer size,
            @RequestParam(required = false) String teamName
    ) {
        log.info("获取可申请团队列表");
        PageResult<TeamAvailableApplyVO> result = teamService.availableApply(page, size, teamName);
        return Result.success(result);
    }
}
