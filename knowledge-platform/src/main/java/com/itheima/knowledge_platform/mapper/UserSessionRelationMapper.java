package com.itheima.knowledge_platform.mapper;

import com.itheima.knowledge_platform.pojo.entity.UserSessionRelation;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserSessionRelationMapper {

    /**
     * 根据用户ID查询管理的会话
     * @param userId
     * @return
     */
    @Select("select * from user_session_relation where user_id=#{userId}")
    List<UserSessionRelation> selectByUserId(Long userId);

    /**
     * 增加信息
     * @param userSessionRelation
     */
    @Insert("insert into user_session_relation (user_id, session_id)\n" +
            "VALUES (#{userId},#{sessionId})")
    void insert(UserSessionRelation userSessionRelation);

    /**
     * 根据会话id查询信息
     * @param sessionId
     * @return
     */
    @Select("select * from user_session_relation where session_id=#{sessionId}")
    UserSessionRelation selectBySessionId(String sessionId);

    @Delete("delete from user_session_relation where session_id=#{sessionId}")
    void deleteBySessionId(String sessionId);
}
