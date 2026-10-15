package com.itheima.knowledge_platform.mapper;

import com.itheima.knowledge_platform.annotation.AutoFill;
import com.itheima.knowledge_platform.enums.TimeOperationEnum;
import com.itheima.knowledge_platform.pojo.entity.ChatSession;
import com.itheima.knowledge_platform.pojo.vo.ChatSessionVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ChatSessionMapper {

    /**
     * 增加数据
      * @param session
     */
    @AutoFill(TimeOperationEnum.INSERT)
    @Insert("insert into chat_session (session_id, session_name, json_path, created_at, updated_at)\n" +
            "VALUES (#{sessionId},#{sessionName},#{jsonPath},#{createdAt},#{updatedAt})")
    void insert(ChatSession session);

    /**
     * 根据主键查询
     * @return
     */
    @Select("select * from chat_session where session_id=#{sessionId}")
    ChatSession selectBySessionId(String sessionId);

    /**
     * 根据用户id查询结合关联表
     * @param userId
     * @return
     */
    List<ChatSessionVO> selectBySessionIdWithUser(Long userId);

    /**
     * 重命名会话
     * @param sessionId
     * @param sessionName
     */
    @Select("update chat_session set session_name=#{sessionName} where session_id=#{sessionId}")
    void updateNameBySessionId(String sessionId, String sessionName);

    @Delete("delete from chat_session where session_id=#{sessionId}")
    void deleteBySessionId(String sessionId);
}
