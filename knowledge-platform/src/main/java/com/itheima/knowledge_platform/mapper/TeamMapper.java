package com.itheima.knowledge_platform.mapper;


import com.github.pagehelper.Page;
import com.itheima.knowledge_platform.pojo.entity.Team;
import com.itheima.knowledge_platform.pojo.inner.user.UserTeam;
import com.itheima.knowledge_platform.pojo.vo.TeamAdminVO;
import com.itheima.knowledge_platform.pojo.vo.TeamAvailableApplyVO;
import com.itheima.knowledge_platform.pojo.vo.TeamPageVO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TeamMapper {

    /**
     * 通过用户id获取是否当过队长
     */
    @Select("select * from team where owner_user_id=#{ownerUserId}")
    List<Team> selectByOwnerUserId(Long ownerUserId);

    /**
     * 添加信息
     * @param team
     */
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    @Insert("insert into team (name, owner_user_id, status, created_at) " +
            "VALUES (#{name},#{ownerUserId},#{status},#{createdAt})")
    void insert(Team team);

    /**
     * 分页查询
     *
     * @param status
     * @param userId
     * @return
     */
    List<TeamPageVO> pageQuery(Integer status, Long userId);

    /**
     * 根据id查询
     * @param id
     * @return
     */
    @Select("select * from team where id=#{id}")
    Team selectById(Long id);

    /**
     *解散队伍
     * @param id
     */
    @Update("update team set status=#{status} where id=#{id}")
    void updateStatus(Long id, Integer status);

    /**
     * 获取用户团队详细
     * @param userId
     * @return
     */
    List<UserTeam> selectUserTeamDetail(Long userId);

    /**
     * 获取所有团队列表（管理端）
     * @param status
     * @param teamName
     * @return
     */
    List<TeamAdminVO> selectTeamAdminVOByStatusAndTeamName(@Min(value = 0, message = "状态值最小为0") @Max(value = 2, message = "状态值最大为2") Integer status, String teamName);

    /**
     * 获取可申请团队列表
     * @param teamName
     * @return
     */
    List<TeamAvailableApplyVO> selectByTeamNameWithStatus(String teamName);
}
