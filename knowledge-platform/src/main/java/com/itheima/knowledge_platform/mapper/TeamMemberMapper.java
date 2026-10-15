package com.itheima.knowledge_platform.mapper;

import com.itheima.knowledge_platform.pojo.entity.TeamMember;
import com.itheima.knowledge_platform.pojo.vo.TeamMemberDetailPageVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TeamMemberMapper {

    /**
     * 根据团队id和用户id查询
     * @param teamId
     * @param userId
     * @return
     */
    @Select("select * from team_member where team_id=#{teamId} and user_id=#{userId}")
    TeamMember selectByTeamIdAndUserId(Long teamId, Long userId);

    /**
     * 根据团队id查询成员列表
     * @param teamId
     * @return
     */
    @Select("select * from team_member where team_id=#{teamId}")
    List<TeamMember> selectTeamMemberByTeamId(Long teamId);

    /**
     * 添加数据
     * @param teamMember
     */
    @Insert("insert into team_member (team_id, user_id, team_role, joined_at)\n" +
            "VALUES (#{teamId},#{userId},#{teamRole},#{joinedAt})")
    void insert(TeamMember teamMember);

    /**
     * 根据userId查询队伍
     * @param userId
     * @return
     */
    @Select("select * from team_member where user_id=#{userId}")
    List<TeamMember> selectTeamByUserId(Long userId);

    /**
     * 根据团队ID结合用户表查询
     * @param teamId
     * @return
     */
    List<TeamMemberDetailPageVO> selectUserDetailByTeamId(Long teamId);

    /**
     * 根据用户ID删除
     * @param userId
     */
    @Delete("delete from team_member where user_id=#{userId}")
    void deleteByUserId(Long userId);

    /**
     * 删除
     * @param teamId
     */
    @Delete("delete from team_member where team_id=#{teamId}")
    void deleteByTeamId(Long teamId);
}
