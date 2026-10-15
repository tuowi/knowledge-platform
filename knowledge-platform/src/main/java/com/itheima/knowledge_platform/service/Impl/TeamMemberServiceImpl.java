package com.itheima.knowledge_platform.service.Impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itheima.knowledge_platform.constant.MessageConstant;
import com.itheima.knowledge_platform.context.BaseContext;
import com.itheima.knowledge_platform.enums.IdentityEnum;
import com.itheima.knowledge_platform.enums.TeamStatusEnum;
import com.itheima.knowledge_platform.exception.AccountNotFoundException;
import com.itheima.knowledge_platform.exception.TeamErrorException;
import com.itheima.knowledge_platform.mapper.*;
import com.itheima.knowledge_platform.pojo.entity.*;
import com.itheima.knowledge_platform.pojo.vo.TeamMemberDetailPageVO;
import com.itheima.knowledge_platform.result.PageResult;
import com.itheima.knowledge_platform.service.TeamMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Base64;
import java.util.List;

@Service
public class TeamMemberServiceImpl implements TeamMemberService {

    @Autowired
    private TeamMapper teamMapper;

    @Autowired
    private TeamMemberMapper teamMemberMapper;
    @Autowired
    private DocumentMapper documentMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private TagMapper tagMapper;
    @Autowired
    private DocumentTagMapper documentTagMapper;
    @Autowired
    private PlatformAdminMapper platformAdminMapper;

    /**
     * 获取团队成员列表
     *
     * @param teamId
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageResult<TeamMemberDetailPageVO> memberPage(Long teamId, Integer page, Integer size) {
        //校验团队是否存在
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new TeamErrorException(MessageConstant.TEAM_NOT_EXIST);
        }

        //校验当前用户是否是该团队的成员（只有成员才能查看成员列表）
        List<TeamMember> teamMembers = teamMemberMapper.selectTeamMemberByTeamId(teamId);

        List<Long> userIds = teamMembers.stream()
                .map(TeamMember::getUserId)
                .toList();

        if (!userIds.contains(BaseContext.getCurrentUserId())) {
            throw new TeamErrorException(MessageConstant.USER_NOT_IN_TEAM);
        }

        //开始分页
        PageHelper.startPage(page, size);

        /**
         * 查询该团队的所有成员信息，包括：
         * 用户基本信息（ID、用户名、头像、邮箱）
         * 成员角色（队长还是普通成员）
         * 加入时间
         */
        //队长在列表中排在首位（按角色排序）
        List<TeamMemberDetailPageVO> list = teamMemberMapper.selectUserDetailByTeamId(team.getId());

        Page<TeamMemberDetailPageVO> page1 = (Page<TeamMemberDetailPageVO>) list;

        List<TeamMemberDetailPageVO> list1 = list.stream().map(teamMemberDetailPageVO -> {
            teamMemberDetailPageVO.setRole(IdentityEnum.getByCode(teamMemberDetailPageVO.getTeamRole()).getMessage());
            return teamMemberDetailPageVO;
        }).toList();

        return new PageResult<>(page1.getTotal(), page1.getPageNum(), page1.getPageSize(), list1);
    }

    /**
     * 移出团队成员（队长）
     *
     * @param teamId
     * @param userId
     */
    @Override
    public void deleteMemberByUserId(Long teamId, Long userId) {
        //查询团队是否存在，不存在返回404
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new TeamErrorException(MessageConstant.TEAM_NOT_EXIST);
        }

        //从token获取当前用户ID，判断是否等于团队的owner_user_id，不是则返回403（只有队长可操作）
        Long currentUserId = BaseContext.getCurrentUserId();
        if (!team.getOwnerUserId().equals(currentUserId)) {
            throw new TeamErrorException(MessageConstant.USER_IS_NOT_LEADER);
        }

        //判断被移出的userId是否等于队长ID，是则返回400（队长不能移出自己）
        if (userId.equals(currentUserId)) {
            throw new TeamErrorException(MessageConstant.NO_PERMISSION_TO_DELETE_MYSELF);
        }

        //查询document表，统计该用户在该团队下有多少文档（author_id = 当前用户ID AND team_id = 团队ID）
        List<Document> documents = documentMapper.selectByAuthorIdAndTeamId(userId, team.getId());

        if (documents.size() > 0) {
            throw new TeamErrorException(MessageConstant.USER_HAS_DOCUMENT);
        }

        //删除team_member表中该用户记录
        teamMemberMapper.deleteByUserId(userId);

        //TODO （可选）调用通知服务，发送TEAM_REMOVE通知给被移出的用户
    }

    /**
     * 退出团队
     *
     * @param teamId
     */
    @Override
    public void exit(Long teamId) {
        //查询团队是否存在，不存在返回404
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new TeamErrorException(MessageConstant.TEAM_NOT_EXIST);
        }

        //从token获取当前用户ID
        Long userId = BaseContext.getCurrentUserId();

        //查询team_member表，判断用户是否在该团队中，不在则返回404
        TeamMember teamMember = teamMemberMapper.selectByTeamIdAndUserId(teamId, userId);
        if (teamMember == null) {
            throw new TeamErrorException(MessageConstant.USER_NOT_IN_TEAM);
        }

        //判断当前用户ID是否等于团队的owner_user_id，是则返回400（队长不能直接退出）
        if (userId == team.getOwnerUserId()) {
            throw new TeamErrorException(MessageConstant.NO_PERMISSION_TO_DELETE_LEADER);
        }
        //查询document表，统计该用户在该团队下有多少文档（author_id = 当前用户ID AND team_id = 团队ID）
        List<Document> documents = documentMapper.selectByAuthorIdAndTeamId(userId, team.getId());

        //如果文档数量 > 0，返回400错误
        if (documents.size() > 0) {
            throw new TeamErrorException(MessageConstant.USER_HAS_DOCUMENT);
        }

        //删除team_member表中该用户记录
        teamMemberMapper.deleteByUserId(userId);

        //TODO （可选）调用通知服务，发送TEAM_QUIT通知给队长
    }

    /**
     * 解散团队（队长）
     * 前置条件：团队名下无任何文档
     *
     * @param teamId
     */
    @Transactional
    @Override
    public void disband(Long teamId) {
        //查询团队是否存在，不存在返回404
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new TeamErrorException(MessageConstant.TEAM_NOT_EXIST);
        }

        //从token获取当前用户ID，判断是否等于团队的owner_user_id，不是则返回403
        Long userId = BaseContext.getCurrentUserId();
        if (userId == team.getOwnerUserId()) {
            throw new TeamErrorException(MessageConstant.USER_IS_NOT_LEADER);
        }

        //查询document表，统计该团队下文档数量（team_id = 团队ID）
        List<Document> documents = documentMapper.selectByTeamId(team.getId());
        //如果文档数量 > 0，返回400错误（前置条件不满足）
        if (!CollectionUtils.isEmpty(documents)) {
            throw new TeamErrorException(MessageConstant.TEAM_HAS_DOCUMENT);
        }

        //删除team_member表中所有该团队ID的记录
        teamMemberMapper.deleteByTeamId(team.getId());

        //（可选）删除该团队下的分类（category表，team_id = 团队ID）
        List<Category> categoryList = categoryMapper.selectByTeamId(teamId);
        for (Category category : categoryList) {
            List<Document> documentList = documentMapper.selectByCategoryId(category.getId());
            if (!documentList.isEmpty()) {
                throw new TeamErrorException(MessageConstant.NO_PERMISSION_TO_DELETE_CATEGORY);
            }
        }
        categoryMapper.deleteByTeamId(team.getId());

        //（可选）删除该团队下的标签（tag表，team_id = 团队ID，注意确认没有其他团队共用）
        List<Tags> tags = tagMapper.selectByTeamId(team.getId());
        if (!tags.isEmpty()) {
            for (Tags tag : tags) {
                List<DocumentTag> documentTags = documentTagMapper.selectByTagId(tag.getId());

                if (!documentTags.isEmpty()) {
                    throw new TeamErrorException(MessageConstant.NO_PERMISSION_TO_DELETE_TAG);
                }
            }
        }

        tagMapper.deleteByTeamId(team.getId());

        //更新团队状态为已解散（status=2）
        teamMapper.updateStatus(team.getId(), TeamStatusEnum.DISBAND.getCode());

        //TODO （可选）调用通知服务，发送TEAM_DISMISS通知给所有成员
    }

    /**
     * 强制解散团队
     * @param id
     */
    @Transactional
    @Override
    public void disbandAdmin(Long id) {
        //查询所有团队（不分管理员权限，超管可见全部）
        Long currentAdminId = BaseContext.getCurrentAdminId();
        PlatformAdmin platformAdmin = platformAdminMapper.selectById(currentAdminId);
        if (platformAdmin == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //检查团队是否存在
        Team team = teamMapper.selectById(id);
        if (team==null){
            throw new TeamErrorException(MessageConstant.TEAM_NOT_EXIST);
        }

        //检查团队名下是否有文档，若有，拒绝解散，提示先清空文档
        List<Document> documents = documentMapper.selectByTeamId(team.getId());
        if (!documents.isEmpty()){
            throw new TeamErrorException(MessageConstant.TEAM_HAS_DOCUMENT);
        }

        //删除团队所有成员关联（team_member表）
        teamMemberMapper.deleteByTeamId(team.getId());

        //（可选）删除该团队下的分类（category表，team_id = 团队ID）
        List<Category> categoryList = categoryMapper.selectByTeamId(team.getId());
        for (Category category : categoryList) {
            List<Document> documentList = documentMapper.selectByCategoryId(category.getId());
            if (!documentList.isEmpty()) {
                throw new TeamErrorException(MessageConstant.NO_PERMISSION_TO_DELETE_CATEGORY);
            }
        }
        categoryMapper.deleteByTeamId(team.getId());

        //（可选）删除该团队下的标签（tag表，team_id = 团队ID，注意确认没有其他团队共用）
        List<Tags> tags = tagMapper.selectByTeamId(team.getId());
        if (!tags.isEmpty()) {
            for (Tags tag : tags) {
                List<DocumentTag> documentTags = documentTagMapper.selectByTagId(tag.getId());

                if (!documentTags.isEmpty()) {
                    throw new TeamErrorException(MessageConstant.NO_PERMISSION_TO_DELETE_TAG);
                }
            }
        }

        //更新团队状态为已解散（status=2）
        teamMapper.updateStatus(team.getId(),TeamStatusEnum.DISBAND.getCode());

        //TODO 通知所有团队成员（存入通知表）
    }
}
