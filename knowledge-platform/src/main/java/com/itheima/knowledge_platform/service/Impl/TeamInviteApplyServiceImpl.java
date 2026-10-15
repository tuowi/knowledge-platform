package com.itheima.knowledge_platform.service.Impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.itheima.knowledge_platform.constant.MessageConstant;
import com.itheima.knowledge_platform.constant.StatusConstant;
import com.itheima.knowledge_platform.context.BaseContext;
import com.itheima.knowledge_platform.enums.IdentityEnum;
import com.itheima.knowledge_platform.enums.TeamInviteApplyStatusEnum;
import com.itheima.knowledge_platform.enums.TeamInviteApplyTypeEnum;
import com.itheima.knowledge_platform.enums.TeamStatusEnum;
import com.itheima.knowledge_platform.exception.AccountNotFoundException;
import com.itheima.knowledge_platform.exception.TeamErrorException;
import com.itheima.knowledge_platform.mapper.TeamInviteApplyMapper;
import com.itheima.knowledge_platform.mapper.TeamMapper;
import com.itheima.knowledge_platform.mapper.TeamMemberMapper;
import com.itheima.knowledge_platform.mapper.UserMapper;
import com.itheima.knowledge_platform.pojo.entity.Team;
import com.itheima.knowledge_platform.pojo.entity.TeamInviteApply;
import com.itheima.knowledge_platform.pojo.entity.TeamMember;
import com.itheima.knowledge_platform.pojo.entity.User;
import com.itheima.knowledge_platform.pojo.vo.*;
import com.itheima.knowledge_platform.properties.PageTeamMemberProperties;
import com.itheima.knowledge_platform.properties.TeamProperties;
import com.itheima.knowledge_platform.result.PageResult;
import com.itheima.knowledge_platform.service.TeamInviteApplyService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeamInviteApplyServiceImpl implements TeamInviteApplyService {

    @Autowired
    private TeamMapper teamMapper;

    @Autowired
    private TeamProperties teamProperties;

    @Autowired
    private TeamInviteApplyMapper teamInviteApplyMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TeamMemberMapper teamMemberMapper;

    @Autowired
    private PageTeamMemberProperties pageTeamMemberProperties;

    /**
     * 获取可邀请的用户列表
     *
     * @param id
     * @param username
     * @return
     */
    @Override
    public PageResult<InvitableUserListVO> invitableList(Integer page, Integer size, Long id, String username) {
        //校验当前用户是否是队长
        Long userId = BaseContext.getCurrentUserId();

        Team team = teamMapper.selectById(id);
        if (!team.getOwnerUserId().equals(userId)) {
            throw new TeamErrorException(MessageConstant.USER_IS_NOT_LEADER);
        }

        //查询所有普通用户（不是管理员）且状态为启用
        User user = User.builder()
                .username(username)
                .status(StatusConstant.ENABLE)
                .build();

        //开始分页
        PageHelper.startPage(page, size);

        //支持按用户名关键词搜索
        List<User> users = userMapper.selectByUser(user);

        // 3. 封装分页信息（此时 PageInfo 里的 List 还是 PO 类型）
        PageInfo<User> userPageInfo = new PageInfo<>(users);

        //搜索该团队队员
        List<TeamMember> teamMembers = teamMemberMapper.selectTeamMemberByTeamId(team.getId());
        List<Long> teamMemberUserIds = teamMembers.stream()
                .map(TeamMember::getUserId)
                .collect(Collectors.toList());

        //搜索已经发过待处理的请求
        List<TeamInviteApply> teamInviteApplyList = teamInviteApplyMapper.selectByTeamId(team.getId());
        List<Long> operatedUserIds = teamInviteApplyList.stream()
                .filter(teamInviteApply -> teamInviteApply.getStatus().equals(TeamInviteApplyStatusEnum.PENDING.getCode()))
                .map(TeamInviteApply::getOperateUserId)
                .toList();

        //排除已经是团队成员的用户
        //排除已经发送过邀请（待处理状态）的用户
        List<InvitableUserListVO> voList = users.stream()
                .filter(user1 -> !teamMemberUserIds.contains(user1.getId()))
                .filter(user3 -> !operatedUserIds.contains(user3.getId()))
                .map(user2 -> {
                    InvitableUserListVO invitableUserListVO = new InvitableUserListVO();
                    BeanUtils.copyProperties(user2, invitableUserListVO);
                    return invitableUserListVO;
                }).collect(Collectors.toList());

        PageResult<InvitableUserListVO> voPage = new PageResult<>();
        voPage.setRecords(voList);
        voPage.setTotal(userPageInfo.getTotal());
        voPage.setPageNum(userPageInfo.getPageNum());
        voPage.setPageSize(userPageInfo.getPageSize());

        return voPage;
    }

    /**
     * 发送入队邀请（队长）
     *
     * @param teamId
     * @param userId
     * @return
     */
    @Override
    public TeamInviteLeaderVO invitationsLeader(Long teamId, Long userId) {
        //权限校验：当前登录用户必须是该团队的队长
        Long currentUserId = BaseContext.getCurrentUserId();

        Team team = teamMapper.selectById(teamId);
        if (!team.getOwnerUserId().equals(currentUserId)) {
            throw new TeamErrorException(MessageConstant.USER_IS_NOT_LEADER);
        }

        //团队状态校验：团队必须存在且未被解散
        if (team.getStatus().equals(TeamStatusEnum.DISBAND.getCode())) {
            throw new TeamErrorException(MessageConstant.TEAM_NOT_FOUND);
        }

        /**
         * 被邀请的用户ID必须 存在 且 启用状态
         * 被邀请的用户不能已经是团队成员
         * 被邀请的用户不能已经有待处理的邀请（同一团队未处理的邀请）
         * （可选）团队人数未达到上限
         */
        //被邀请的用户ID必须 存在 且 启用状态
        User user = userMapper.getById(userId);

        if (user == null || user.getStatus().equals(StatusConstant.DISABLE)) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //被邀请的用户不能已经是团队成员
        TeamMember teamMember = teamMemberMapper.selectByTeamIdAndUserId(team.getId(), user.getId());
        if (teamMember != null) {
            throw new TeamErrorException(MessageConstant.USER_IS_IN_TEAM);
        }

        //被邀请的用户不能已经有待处理的邀请（同一团队未处理的邀请）
        TeamInviteApply builder = TeamInviteApply.builder()
                .teamId(team.getId())
                .operateUserId(user.getId())
                .status(TeamInviteApplyStatusEnum.PENDING.getCode())
                .type(TeamInviteApplyTypeEnum.CAPTAIN_INVITE_USER.getCode())
                .build();
        TeamInviteApply teamInviteApply = teamInviteApplyMapper.selectByTeamIdAndUserIdAndStatus(builder);

        if (teamInviteApply != null) {
            throw new TeamErrorException(MessageConstant.ALREADY_INVITE);
        }

        //（可选）团队人数未达到上限
        int memberCount = teamMemberMapper.selectTeamMemberByTeamId(team.getId()).size();
        if (memberCount >= teamProperties.getMaxTeamMember()) {
            throw new TeamErrorException(MessageConstant.TEAM_MEMBER_OVER);
        }

        //创建邀请记录：在邀请表中插入一条记录，状态为"待处理"
        TeamInviteApply inviteApply = TeamInviteApply.builder()
                .teamId(team.getId())
                .applyUserId(currentUserId)
                .operateUserId(userId)
                .type(TeamInviteApplyTypeEnum.CAPTAIN_INVITE_USER.getCode())
                .status(TeamInviteApplyStatusEnum.PENDING.getCode())
                .createdAt(LocalDateTime.now())
                .build();
        teamInviteApplyMapper.insert(inviteApply);

        //TODO 发送通知：
        //如果被邀请用户在线，通过WebSocket实时推送通知
        //同时存入数据库通知表，供用户离线后查看


        TeamInviteLeaderVO teamInviteLeaderVO = new TeamInviteLeaderVO();
        BeanUtils.copyProperties(inviteApply, teamInviteLeaderVO);

        teamInviteLeaderVO.setTeamName(teamMapper.selectById(team.getId()).getName());
        teamInviteLeaderVO.setOperateUserName(userMapper.getById(user.getId()).getUsername());
        teamInviteLeaderVO.setStatusName(TeamInviteApplyStatusEnum.getByCode(inviteApply.getStatus()).getMessage());
        teamInviteLeaderVO.setExpireAt(inviteApply.getCreatedAt().plusDays(teamProperties.getExpired()));

        return teamInviteLeaderVO;
    }

    /**
     * 获取我的入队邀请列表
     * 查询当前登录用户收到的所有入队邀请，包括待处理、已同意、已拒绝的邀请记录。
     *
     * @param page
     * @param size
     * @param status
     * @return
     */
    @Override
    public PageResult<TeamInviteApplyPageVO> pageInvitations(Integer page, Integer size, Integer status) {
        //身份识别：从 token 中获取当前登录用户的 ID
        Long userId = BaseContext.getCurrentUserId();

        //分页处理：支持分页返回
        PageHelper.startPage(pageTeamMemberProperties.getPageNum(), pageTeamMemberProperties.getPageSize());

        //查询数据库：从入队邀请表中查询 invitee_id（被邀请人ID）等于当前用户ID 的所有记录
        List<TeamInviteApply> teamInviteApplyList = teamInviteApplyMapper.selectByOperatedUserIdAndStatus(userId, status);

        Page<TeamInviteApply> teamInviteApplies = (Page<TeamInviteApply>) teamInviteApplyList;

        //关联查询：需要连表查出团队名称、邀请人姓名（发送邀请的队长）
        List<TeamInviteApplyPageVO> list = teamInviteApplyList.stream()
                .map(teamInviteApply -> {
                    TeamInviteApplyPageVO teamInviteApplyPageVO = new TeamInviteApplyPageVO();
                    BeanUtils.copyProperties(teamInviteApply, teamInviteApplyPageVO);

                    teamInviteApplyPageVO.setTeamName(teamMapper.selectById(teamInviteApply.getTeamId()).getName());
                    teamInviteApplyPageVO.setApplyUserName(userMapper.getById(teamInviteApply.getApplyUserId()).getUsername());

                    return teamInviteApplyPageVO;
                }).collect(Collectors.toList());

        PageResult<TeamInviteApplyPageVO> listPage = new PageResult<>();
        listPage.setRecords(list);
        listPage.setTotal(teamInviteApplies.getTotal());
        listPage.setPageNum(teamInviteApplies.getPageNum());
        listPage.setPageSize(teamInviteApplies.getPageSize());

        return new PageResult<>(listPage.getTotal(), listPage.getPageNum(), listPage.getPageSize(), listPage.getRecords());
    }

    /**
     * 处理入队邀请（同意/拒绝）
     *
     * @param invitationId
     * @param status
     */
    @Transactional
    @Override
    public void handleInvitations(Long invitationId, Integer status) {
        //校验邀请记录存在：根据 invitationId 查询邀请记录是否存在
        TeamInviteApply teamInviteApply = teamInviteApplyMapper.selectById(invitationId);

        if (teamInviteApply == null) {
            throw new TeamErrorException(MessageConstant.INVITE_NOT_FOUND);
        }

        //校验接收人身份：当前登录用户必须是这条邀请记录的 target_user_id（被邀请人），不能替别人处理
        Long userId = BaseContext.getCurrentUserId();

        if (!teamInviteApply.getOperateUserId().equals(userId)) {
            throw new TeamErrorException(MessageConstant.NO_PERMISSION_TO_HANDLE_INVITATION);
        }

        //校验邀请状态：只有"待处理"状态的邀请才能被处理，已处理过的不能重复处理
        if (!teamInviteApply.getStatus().equals(TeamInviteApplyStatusEnum.PENDING.getCode())) {
            throw new TeamErrorException(MessageConstant.INVITATION_HAS_BEEN_PROCESSED);
        }

        //校验邀请是否过期：如果设置了邀请有效期（如7天），超过有效期不能处理
        if (teamInviteApply.getCreatedAt().plusDays(teamProperties.getExpired()).isBefore(LocalDateTime.now())) {
            //已经过期，不能处理
            throw new TeamErrorException(MessageConstant.INVITATION_EXPIRED);
        }

        /**
         * 根据 status 执行不同逻辑：
         * 同意（status=1）：
         * 检查团队当前人数是否已达上限
         * 检查用户是否已经在该团队中（防止重复加入）
         * 将用户添加到团队的成员表
         * 拒绝（status=2）：
         * 不需要操作团队成员，直接更新邀请状态即可
         */
        if (status.equals(TeamInviteApplyStatusEnum.AGREE.getCode())) {
            //同意
            //检查团队当前人数是否已达上限
            List<TeamMember> teamMembers = teamMemberMapper.selectTeamMemberByTeamId(teamInviteApply.getTeamId());

            if (teamMembers.size() >= teamProperties.getMaxTeamMember()) {
                throw new TeamErrorException(MessageConstant.TEAM_IS_FULL);
            }

            //检查用户是否已经在该团队中（防止重复加入）
            List<Long> list = teamMembers.stream().map(TeamMember::getUserId).toList();
            if (list.contains(userId)) {
                throw new TeamErrorException(MessageConstant.USER_IS_IN_TEAM);
            }

            //将用户添加到团队的成员表
            TeamMember teamMember = TeamMember.builder()
                    .userId(userId)
                    .teamId(teamInviteApply.getTeamId())
                    .teamRole(IdentityEnum.NORMAL.getCode())
                    .joinedAt(LocalDateTime.now())
                    .build();
            teamMemberMapper.insert(teamMember);

            //若团队人数达标，团队状态改变
            if (teamMemberMapper.selectTeamMemberByTeamId(teamInviteApply.getTeamId()).size() > 1) {
                teamMapper.updateStatus(teamInviteApply.getTeamId(), TeamStatusEnum.CREATED.getCode());
            }
        }

        //拒绝（status=2）：
        //不需要操作团队成员，直接更新邀请状态即可
        teamInviteApply.setStatus(status);
        teamInviteApply.setHandleAt(LocalDateTime.now());
        teamInviteApplyMapper.updateStatusById(teamInviteApply);
    }

    /**
     * 主动申请加入团队
     *
     * @param id
     */
    @Override
    public void apply(Long id) {
        //校验团队存在：根据团队ID查询团队是否存在，不存在则返回错误
        Team team = teamMapper.selectById(id);
        if (team == null) {
            throw new TeamErrorException(MessageConstant.TEAM_NOT_EXIST);
        }

        //校验团队状态：团队是否已解散或禁用，如果是则不允许申请
        if (team.getStatus().equals(TeamStatusEnum.DISBAND.getCode())) {
            throw new TeamErrorException(MessageConstant.TEAM_NOT_FOUND);
        }

        /**
         * 校验用户身份：
         * 用户是否已经是该团队成员，如果是则不能重复申请
         * 用户是否已经是队长，队长不需要申请
         */
        TeamMember teamMember = teamMemberMapper.selectByTeamIdAndUserId(team.getId(), BaseContext.getCurrentUserId());

        if (teamMember != null) {
            if (teamMember.getTeamRole().equals(IdentityEnum.NORMAL.getCode())) {
                throw new TeamErrorException(MessageConstant.USER_IS_IN_TEAM);
            } else {
                throw new TeamErrorException(MessageConstant.USER_NOT_LEADER);
            }
        }

        //校验重复申请：查询是否有该用户的待处理申请（状态为待审核），如果有则提示“已有待审核的申请，请勿重复提交”
        TeamInviteApply inviteApply = TeamInviteApply.builder()
                .teamId(team.getId())
                .operateUserId(team.getOwnerUserId())
                .status(TeamInviteApplyStatusEnum.PENDING.getCode())
                .type(TeamInviteApplyTypeEnum.USER_APPLY_TEAM.getCode())
                .build();
        TeamInviteApply teamInviteApply = teamInviteApplyMapper.selectByTeamIdAndUserIdAndStatus(inviteApply);

        if (teamInviteApply != null) {
            throw new TeamErrorException(MessageConstant.ALREADY_INVITE);
        }

        //创建申请记录：插入一条入队申请记录，状态为“待审核”
        TeamInviteApply build = TeamInviteApply.builder()
                .teamId(team.getId())
                .type(TeamInviteApplyTypeEnum.USER_APPLY_TEAM.getCode())
                .createdAt(LocalDateTime.now())
                .applyUserId(BaseContext.getCurrentUserId())
                .operateUserId(team.getOwnerUserId())
                .status(TeamInviteApplyStatusEnum.PENDING.getCode())
                .build();
        teamInviteApplyMapper.insert(build);

        //TODO 通知队长：通过WebSocket实时通知团队队长（如果队长在线），同时存入数据库通知表
    }

    /**
     * 获取我的申请列表
     *
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageResult<TeamApplyPageVO> pageApply(Integer page, Integer size) {
        //获取当前用户ID：从登录信息中获取
        Long userId = BaseContext.getCurrentUserId();

        PageHelper.startPage(page, size);

        //查询申请记录：根据用户ID查询入队申请表，按创建时间倒序
        List<TeamInviteApply> teamInviteApplyList = teamInviteApplyMapper.selectByApplyUserIdAndType(userId, TeamInviteApplyTypeEnum.USER_APPLY_TEAM.getCode());

        PageInfo<TeamInviteApply> teamInviteApplyPageInfo = new PageInfo<>(teamInviteApplyList);

        //关联团队信息：需要连表查询团队名称
        List<TeamApplyPageVO> list = teamInviteApplyList.stream()
                .map(teamInviteApply -> {
                    TeamApplyPageVO teamApplyPageVO = new TeamApplyPageVO();
                    BeanUtils.copyProperties(teamInviteApply, teamApplyPageVO);

                    teamApplyPageVO.setTeamName(teamMapper.selectById(teamInviteApply.getTeamId()).getName());
                    teamApplyPageVO.setStatusName(TeamInviteApplyStatusEnum.getByCode(teamInviteApply.getStatus()).getMessage());

                    return teamApplyPageVO;
                }).collect(Collectors.toList());

        PageResult<TeamApplyPageVO> voPage = new PageResult<>();
        voPage.setRecords(list);
        voPage.setTotal(teamInviteApplyPageInfo.getTotal());
        voPage.setPageNum(teamInviteApplyPageInfo.getPageNum());
        voPage.setPageSize(teamInviteApplyPageInfo.getPageSize());

        return voPage;
    }


    /**
     * 处理入队申请（队长）
     *
     * @param applicationId
     * @param status
     */
    @Transactional
    @Override
    public void applicationLeader(Long applicationId, Integer status) {
        Long userId = BaseContext.getCurrentUserId();

        //校验申请存在：根据申请ID查询申请记录，不存在则返回错误
        TeamInviteApply teamInviteApply = teamInviteApplyMapper.selectById(applicationId);

        if (teamInviteApply == null) {
            throw new TeamErrorException(MessageConstant.INVITE_NOT_FOUND);
        }

        //校验申请状态：申请状态必须是“待审核”，已处理过的不能重复处理
        if (!teamInviteApply.getStatus().equals(TeamInviteApplyStatusEnum.PENDING.getCode())) {
            throw new TeamErrorException(MessageConstant.INVITATION_HAS_BEEN_PROCESSED);
        }

        //校验操作权限：当前用户必须是该团队的队长，否则返回403无权限
        Team team = teamMapper.selectById(teamInviteApply.getTeamId());
        if (!team.getOwnerUserId().equals(userId)) {
            throw new TeamErrorException(MessageConstant.USER_IS_NOT_LEADER);
        }

        /**
         * 校验团队状态（同意时）：
         * 团队是否存在且未解散
         * 团队人数是否已满（如设置上限）
         * 用户是否已经被加入（防止重复加入）
         */
        if (status.equals(TeamInviteApplyStatusEnum.AGREE.getCode())) {
            //团队是否存在且未解散
            if (team == null || team.getStatus().equals(TeamStatusEnum.DISBAND.getCode())) {
                throw new TeamErrorException(MessageConstant.TEAM_NOT_FOUND);
            }

            //团队人数是否已满（如设置上限）
            List<TeamMember> teamMembers = teamMemberMapper.selectTeamMemberByTeamId(team.getId());
            if (teamMembers.size() >= teamProperties.getMaxTeamMember()) {
                throw new TeamErrorException(MessageConstant.TEAM_IS_FULL);
            }

            //用户是否已经被加入（防止重复加入）
            List<Long> list = teamMembers.stream()
                    .map(TeamMember::getUserId)
                    .toList();
            if (list.contains(userId)) {
                throw new TeamErrorException(MessageConstant.USER_IS_IN_TEAM);
            }
        }

        /**
         * 执行处理：
         * 同意（status=1）：
         * 将用户添加到团队成员表
         * 更新申请状态为“已同意”
         * 更新用户的身份字段（如果业务需要标记为普通成员）
         * 通过WebSocket通知申请人申请被同意
         * 存入数据库通知表
         *
         * 拒绝（status=2）：
         * 更新申请状态为“已拒绝”
         * 记录拒绝原因
         * 通过WebSocket通知申请人申请被拒绝（附拒绝理由）
         * 存入数据库通知表
         */
        if (status.equals(TeamInviteApplyStatusEnum.AGREE.getCode())) {
            //将用户添加到团队成员表
            TeamMember teamMember1 = TeamMember.builder()
                    .userId(userId)
                    .teamId(team.getId())
                    .teamRole(IdentityEnum.NORMAL.getCode())
                    .joinedAt(LocalDateTime.now())
                    .build();

            teamMemberMapper.insert(teamMember1);

            //若团队人数达标，团队状态改变
            if (teamMemberMapper.selectTeamMemberByTeamId(teamInviteApply.getTeamId()).size() > 1) {
                teamMapper.updateStatus(teamInviteApply.getTeamId(), TeamStatusEnum.CREATED.getCode());
            }
        }

        //TODO 通过WebSocket通知申请人申请被同意或者被拒绝

        //更新申请状态
        teamInviteApply.setStatus(status);
        teamInviteApply.setHandleAt(LocalDateTime.now());
        teamInviteApplyMapper.updateStatusById(teamInviteApply);

    }

    /**
     * 获取团队待处理的申请列表（队长）
     *
     * @param teamId
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageResult<TeamApplyLeaderPageVO> applyLeaderPage(Long teamId, Integer page, Integer size) {
        //校验团队存在：团队ID对应的团队存在
        Team team = teamMapper.selectById(teamId);

        if (team == null) {
            throw new TeamErrorException(MessageConstant.TEAM_NOT_EXIST);
        }

        //校验操作权限：当前用户必须是该团队的队长，否则返回403无权限
        if (!team.getOwnerUserId().equals(BaseContext.getCurrentUserId())) {
            throw new TeamErrorException(MessageConstant.USER_IS_NOT_LEADER);
        }

        //开始分页
        PageHelper.startPage(page, size);

        //查询待审核申请：查询该团队下状态为“待审核”（status=0）的入队申请记录
        List<TeamInviteApply> teamInviteApplies = teamInviteApplyMapper.selectByStatusAndTeamIdAndType(team.getId(), TeamInviteApplyStatusEnum.PENDING.getCode(), TeamInviteApplyTypeEnum.USER_APPLY_TEAM.getCode());

        PageInfo<TeamInviteApply> pageInfo = new PageInfo<>(teamInviteApplies);

        //关联用户信息：需要连表查询申请人用户名、头像等信息
        List<TeamApplyLeaderPageVO> collect = teamInviteApplies.stream()
                .map(teamInviteApply -> {
                    User user = userMapper.getById(teamInviteApply.getApplyUserId());

                    TeamApplyLeaderPageVO vo = TeamApplyLeaderPageVO.builder()
                            .id(teamInviteApply.getId())
                            .userId(teamInviteApply.getApplyUserId())
                            .userName(user.getUsername())
                            .userAvatar(user.getAvatar())
                            .createdAt(teamInviteApply.getCreatedAt())
                            .build();

                    return vo;
                }).collect(Collectors.toList());

        PageResult<TeamApplyLeaderPageVO> voPage = new PageResult<>();
        voPage.setTotal(pageInfo.getTotal());
        voPage.setPageNum(pageInfo.getPageNum());
        voPage.setPageSize(pageInfo.getPageSize());
        voPage.setRecords(collect);

        return voPage;
    }
}
