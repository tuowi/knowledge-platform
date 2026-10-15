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
import com.itheima.knowledge_platform.mapper.*;
import com.itheima.knowledge_platform.pojo.dto.TeamQueryRequestDTO;
import com.itheima.knowledge_platform.pojo.entity.*;
import com.itheima.knowledge_platform.pojo.vo.*;
import com.itheima.knowledge_platform.properties.PageTeamMemberProperties;
import com.itheima.knowledge_platform.properties.TeamProperties;
import com.itheima.knowledge_platform.result.PageResult;
import com.itheima.knowledge_platform.service.TeamService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TeamServiceImpl implements TeamService {

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
    @Autowired
    private PlatformAdminMapper platformAdminMapper;
    @Autowired
    private DocumentMapper documentMapper;

    /**
     * 创建团队
     *
     * @param name
     * @param inviteUserIds
     * @return
     */
    @Transactional
    @Override
    public TeamVO create(String name, List<Long> inviteUserIds) {
        //校验用户是否有权限创建团队（如一个用户最多创建3个团队）
        List<Team> teams = teamMapper.selectByOwnerUserId(BaseContext.getCurrentUserId());

        if (teams.size() >= teamProperties.getMaxCreatedTeam()) {
            throw new TeamErrorException(MessageConstant.OWNER_TEAM_FULL);
        }

        //校验团队名称是否重复（同一用户下不能重名）
        List<Team> teamList = teams.stream()
                .filter(team -> team.getName().equals(name))
                .collect(Collectors.toList());

        if (teamList.size() > 0) {
            throw new TeamErrorException(MessageConstant.TEAM_NAME_EXIST);
        }

        //创建团队记录，状态设为0（招募中），队长设为当前用户
        Team team = new Team();
        team.setStatus(TeamStatusEnum.PHILTRUM.getCode());
        team.setOwnerUserId(BaseContext.getCurrentUserId());
        team.setName(name);
        team.setCreatedAt(LocalDateTime.now());

        teamMapper.insert(team);

        //创建团队成员关联表记录，队长加入记录
        TeamMember teamMember = TeamMember.builder()
                .teamId(team.getId())
                .userId(team.getOwnerUserId())
                .teamRole(IdentityEnum.TEAM_LEADER.getCode())
                .joinedAt(LocalDateTime.now())
                .build();

        teamMemberMapper.insert(teamMember);

        //遍历邀请用户ID列表，为每个被邀请人创建一条邀请记录（状态为待处理）
        if (!CollectionUtils.isEmpty(inviteUserIds)) {
            for (Long inviteUserId : inviteUserIds) {
                TeamInviteApply inviteApply = TeamInviteApply.builder()
                        .createdAt(LocalDateTime.now())
                        .teamId(team.getId())
                        .applyUserId(team.getOwnerUserId())
                        .operateUserId(inviteUserId)
                        .status(TeamInviteApplyStatusEnum.PENDING.getCode())
                        .type(TeamInviteApplyTypeEnum.CAPTAIN_INVITE_USER.getCode())
                        .build();

                teamInviteApplyMapper.insert(inviteApply);
            }
        }

        //TODO 给所有被邀请人发送入队邀请通知

        //返回团队信息
        //根据队长id查询队长账号名称
        User user = userMapper.getById(team.getOwnerUserId());

        if (user == null) {
            throw new AccountNotFoundException("[create]" + MessageConstant.ACCOUNT_NOT_FOUND);
        }
        //查询成员数量
        List<TeamMember> teamMembers = teamMemberMapper.selectTeamMemberByTeamId(team.getId());

        Integer count = teamMembers.size();

        TeamVO teamVO = TeamVO.builder()
                .id(team.getId())
                .ownerUserId(team.getOwnerUserId())
                .name(team.getName())
                .status(team.getStatus())
                .statusName(TeamStatusEnum.getByCode(team.getStatus()).getMessage())
                .createdAt(team.getCreatedAt())
                .ownerUserName(user.getUsername())
                .memberCount(count)
                .build();

        return teamVO;

    }

    /**
     * 获取团队列表
     *
     * @param page
     * @param size
     * @param status
     * @return
     */
    @Override
    public PageResult<TeamPageVO> page(Integer page, Integer size, Integer status) {
        PageHelper.startPage(page, size);

        //查询当前用户作为成员的所有团队（通过成员关联表）
        //根据状态筛选（如果传了status参数）
        List<TeamPageVO> teamList = teamMapper.pageQuery(status, BaseContext.getCurrentUserId());

        Page<TeamPageVO> teamPageVOS = (Page<TeamPageVO>) teamList;

        List<TeamPageVO> vos = teamList.stream().map(teamPageVO -> {

            teamPageVO.setStatusName(TeamStatusEnum.getByCode(teamPageVO.getStatus()).getMessage());
            teamPageVO.setOwnerUserName(userMapper.getById(teamPageVO.getOwnerUserId()).getUsername());
            teamPageVO.setMemberCount(teamMemberMapper.selectTeamMemberByTeamId(teamPageVO.getId()).size());
            teamPageVO.setTeamRoleName(IdentityEnum.getByCode(teamPageVO.getTeamRole()).getMessage());

            return teamPageVO;
        }).collect(Collectors.toList());

        return new PageResult<>(teamPageVOS.getTotal(), teamPageVOS.getPageNum(), teamPageVOS.getPageSize(), vos);
    }

    /**
     * 获取团队详情
     *
     * @param id
     * @return
     */
    @Override
    public TeamDetailVO teamDetail(Long id) {
        //校验用户是否有权限查看该团队（必须是团队成员或队长）
        Long userId = BaseContext.getCurrentUserId();

        //获取是否有权限
        TeamMember teamMember = teamMemberMapper.selectByTeamIdAndUserId(id, userId);

        if (teamMember == null) {
            throw new TeamErrorException(MessageConstant.USER_NOT_IN_TEAM);
        }

        //查询团队基本信息
        Team team = teamMapper.selectById(id);

        //类型转换
        TeamDetailVO teamDetailVO = new TeamDetailVO();

        BeanUtils.copyProperties(team, teamDetailVO);
        teamDetailVO.setStatsName(TeamStatusEnum.getByCode(team.getStatus()).getMessage());
        teamDetailVO.setOwnerUserName(userMapper.getById(team.getOwnerUserId()).getUsername());
        teamDetailVO.setMemberCount(teamMemberMapper.selectTeamMemberByTeamId(team.getId()).size());

        //查询成员列表（前5个或分页）
        //默认分页
        //PageHelper.startPage() 只对它后面的【第一次】MyBatis 查询方法生效
        PageHelper.startPage(pageTeamMemberProperties.getPageNum(), pageTeamMemberProperties.getPageSize());
        List<TeamMember> teamMembers = teamMemberMapper.selectTeamMemberByTeamId(team.getId());

        //类型转换-----分页
        // PageInfo 会自动包装，包含所有分页信息
        // 3. 封装为 PageInfo
        //PageInfo<TeamMember> teamMemberPage = new PageInfo<>(teamMembers);

        //转换VO
        List<TeamMemberDetailVO> voList = teamMembers.stream()
                .map(po -> {
                    TeamMemberDetailVO vo = new TeamMemberDetailVO();
                    BeanUtils.copyProperties(po, vo);

                    vo.setUsername(userMapper.getById(vo.getUserId()).getUsername());
                    vo.setRole(IdentityEnum.getByCode(vo.getTeamRole()).getMessage());

                    return vo;
                }).collect(Collectors.toList());

        teamDetailVO.setMembers(voList);

        //查询邀请记录状态（队长可见）
        //检验是否是队长
        if (team.getOwnerUserId().equals(userId)) {
            List<TeamInviteApply> teamInviteApplyList = teamInviteApplyMapper.selectByTeamId(team.getId());
            List<TeamInviteMemberDetailVO> teamInviteMemberDetailVOS = teamInviteApplyList.stream()
                    .map(teamInviteApply -> {
                        TeamInviteMemberDetailVO teamInviteMemberDetailVO = new TeamInviteMemberDetailVO();
                        BeanUtils.copyProperties(teamInviteApply, teamInviteMemberDetailVO);

                        teamInviteMemberDetailVO.setUserId(teamInviteApply.getOperateUserId());
                        teamInviteMemberDetailVO.setUsername(userMapper.getById(teamInviteApply.getOperateUserId()).getUsername());
                        teamInviteMemberDetailVO.setStatusName(TeamInviteApplyStatusEnum.getByCode(teamInviteMemberDetailVO.getStatus()).getMessage());

                        return teamInviteMemberDetailVO;
                    }).collect(Collectors.toList());

            teamDetailVO.setPendingInvitations(teamInviteMemberDetailVOS);
        }

        return teamDetailVO;
    }

    /**
     * 获取所有团队列表
     *
     * @param teamQueryRequestDTO
     * @return
     */
    @Override
    public PageResult<TeamAdminVO> pageAdmin(TeamQueryRequestDTO teamQueryRequestDTO) {
        //查询所有团队（不分管理员权限，超管可见全部）
        Long currentAdminId = BaseContext.getCurrentAdminId();
        PlatformAdmin platformAdmin = platformAdminMapper.selectById(currentAdminId);
        if (platformAdmin == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        PageHelper.startPage(teamQueryRequestDTO.getPage(), teamQueryRequestDTO.getSize());

        List<TeamAdminVO> teamAdminVOS = teamMapper.selectTeamAdminVOByStatusAndTeamName(teamQueryRequestDTO.getStatus(), teamQueryRequestDTO.getKeyword());

        Page<TeamAdminVO> voPage = (Page<TeamAdminVO>) teamAdminVOS;

        return new PageResult<>(voPage.getTotal(), voPage.getPageNum(), voPage.getPageSize(), voPage.getResult());
    }

    /**
     * 获取团队详情
     *
     * @param id
     * @return
     */
    @Override
    public TeamAdminDetailVO teamDetailAdmin(Long id) {
        //查询所有团队（不分管理员权限，超管可见全部）
        Long currentAdminId = BaseContext.getCurrentAdminId();
        PlatformAdmin platformAdmin = platformAdminMapper.selectById(currentAdminId);
        if (platformAdmin == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //类型转换
        TeamAdminDetailVO teamAdminDetailVO = new TeamAdminDetailVO();

        //查询团队基本信息
        Team team = teamMapper.selectById(id);
        BeanUtils.copyProperties(team, teamAdminDetailVO);

        //关联查询队长信息（用户名、邮箱）
        User user = userMapper.getById(team.getOwnerUserId());
        teamAdminDetailVO.setOwnerName(user.getUsername());
        teamAdminDetailVO.setOwnerEmail(user.getEmail());

        //统计成员数量
        List<TeamMember> teamMembers = teamMemberMapper.selectTeamMemberByTeamId(team.getId());
        teamAdminDetailVO.setMemberCount(teamMembers.size());

        //统计团队下文档总数（包括私有和团队内）
        List<Document> documents = documentMapper.selectByTeamId(team.getId());
        teamAdminDetailVO.setDocumentCount(documents.size());

        //查询团队成员列表（前10个）
        Integer count = 0;
        List<TeamAdminDetailVO.TeamMember> list = new ArrayList<>();

        for (TeamMember teamMember : teamMembers) {
            if (count == 10)
                break;


            User user1 = userMapper.getById(teamMember.getUserId());

            TeamAdminDetailVO.TeamMember member = new TeamAdminDetailVO.TeamMember();
            member.setUserId(teamMember.getUserId());  // 或者 teamMember.getUserId()
            member.setUsername(user1.getUsername());
            member.setRole(IdentityEnum.getByCode(teamMember.getTeamRole()).getMessage());
            list.add(member);

            count++;
        }

        teamAdminDetailVO.setMembers(list);

        return teamAdminDetailVO;
    }

    /**
     * 获取可申请团队列表
     *
     * @param page
     * @param size
     * @param teamName
     * @return
     */
    @Override
    public PageResult<TeamAvailableApplyVO> availableApply(Integer page, Integer size, String teamName) {
        //获取当前用户ID
        Long userId = BaseContext.getCurrentUserId();

        //排除已加入的团队：查询 team_member 表，获取用户已加入的团队ID列表
        List<TeamMember> teamMembers = teamMemberMapper.selectTeamByUserId(userId);
        List<Long> teamIds = teamMembers.stream()
                .map(TeamMember::getTeamId)
                .toList();

        //团队状态为"正常"（status = 0,1）
        //团队未解散
        //团队ID不在用户已加入的列表中
        PageHelper.startPage(page, size);
        List<TeamAvailableApplyVO> list = teamMapper.selectByTeamNameWithStatus(teamName);

        Page<TeamAvailableApplyVO> voPage = (Page<TeamAvailableApplyVO>) list;

        List<TeamAvailableApplyVO> vos = list.stream().map(po -> {
            int count = teamMemberMapper.selectTeamMemberByTeamId(po.getId()).size();
            po.setMemberCount(count);

            return po;
        }).filter(po1 -> !teamIds.contains(po1.getId())).toList();

        return new PageResult<>(voPage.getTotal(), voPage.getPageNum(), voPage.getPageSize(), vos);
    }
}
