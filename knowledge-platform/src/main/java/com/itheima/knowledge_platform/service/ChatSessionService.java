package com.itheima.knowledge_platform.service;

import com.itheima.knowledge_platform.pojo.vo.ChatSessionDetailVO;
import com.itheima.knowledge_platform.pojo.vo.ChatSessionVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ChatSessionService {

    /**
     * 创建新会话
     * @param sessionName
     * @return
     */
    Map<String, Object> create(String sessionName);

    /**
     * 获取我的会话列表
     * @return
     */
    List<ChatSessionVO> list();

    /**
     * 发送消息
     * @param sessionId
     * @param message
     * @param emitter
     */
    void sendQuery(String sessionId, String message, SseEmitter emitter);

    /**
     * 重命名会话
     * @param sessionId
     * @param sessionName
     */
    void updateName(String sessionId, String sessionName);

    /**
     * 删除会话
     * @param sessionId
     */
    void delete(String sessionId) throws IOException;

    /**
     * 显示会话记录信息
     * @param sessionId
     * @return
     */
    ChatSessionDetailVO showMessages(String sessionId);
}
