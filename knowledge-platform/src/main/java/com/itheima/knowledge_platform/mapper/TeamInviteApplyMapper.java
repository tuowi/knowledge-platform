package com.itheima.knowledge_platform.mapper;

import com.itheima.knowledge_platform.enums.TeamInviteApplyTypeEnum;
import com.itheima.knowledge_platform.pojo.entity.TeamInviteApply;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TeamInviteApplyMapper {

    /**
     * 增加信息
     * @param teamInviteApply
     */
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    @Insert("insert into team_invite_apply (team_id, apply_user_id, operate_user_id, status, type, created_at) " +
            "VALUES (#{teamId},#{applyUserId},#{operateUserId},#{status},#{type},#{createdAt})")
    void insert(TeamInviteApply teamInviteApply);

    /**
     * 根据团队id查询
     * @param teamId
     * @return
     */
    @Select("select * from team_invite_apply where team_id=#{teamId}")
    List<TeamInviteApply> selectByTeamId(Long teamId);

    @Select("select * from team_invite_apply where team_id=#{teamId} and operate_user_id=#{operateUserId} and status=#{status} and type=#{type}")
    TeamInviteApply selectByTeamIdAndUserIdAndStatus(TeamInviteApply teamInviteApply);

    /**
     * 根据被邀请者id和状态动态查询
     * @param userId
     * @param status
     * @return
     */
    List<TeamInviteApply> selectByOperatedUserIdAndStatus(Long userId, Integer status);

    @Select("select * from team_invite_apply where id=#{id}")
    TeamInviteApply selectById(Long id);

    @Update("update team_invite_apply set status=#{status} ,handle_at=#{handleAt} where id=#{id}")
    void updateStatusById(TeamInviteApply teamInviteApply);

    /**
     * 根据类型和申请者ID查询
     * @param applyUserId
     * @param type
     * @return
     */
    @Select("select * from team_invite_apply where apply_user_id=#{applyUserId} and type=#{type} order by created_at desc")
    List<TeamInviteApply> selectByApplyUserIdAndType(Long applyUserId, Integer type);

    @Select("select * from team_invite_apply where team_id=#{teamId} and status=#{status} and type=#{type} order by created_at desc")
    List<TeamInviteApply> selectByStatusAndTeamIdAndType(Long teamId, Integer status, Integer type);
}
