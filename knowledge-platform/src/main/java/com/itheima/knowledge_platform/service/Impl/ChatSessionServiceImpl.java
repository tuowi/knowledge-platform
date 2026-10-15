package com.itheima.knowledge_platform.service.Impl;

import com.itheima.knowledge_platform.constant.MessageConstant;
import com.itheima.knowledge_platform.context.BaseContext;
import com.itheima.knowledge_platform.exception.AccountNotFoundException;
import com.itheima.knowledge_platform.exception.ChatSessionException;
import com.itheima.knowledge_platform.mapper.ChatSessionMapper;
import com.itheima.knowledge_platform.mapper.UserMapper;
import com.itheima.knowledge_platform.mapper.UserSessionRelationMapper;
import com.itheima.knowledge_platform.pojo.entity.ChatSession;
import com.itheima.knowledge_platform.pojo.entity.User;
import com.itheima.knowledge_platform.pojo.entity.UserSessionRelation;
import com.itheima.knowledge_platform.pojo.inner.chatsession.ChatSessionMessage;
import com.itheima.knowledge_platform.pojo.vo.ChatSessionDetailVO;
import com.itheima.knowledge_platform.pojo.vo.ChatSessionVO;
import com.itheima.knowledge_platform.properties.AIUrlProperties;
import com.itheima.knowledge_platform.properties.FilePathProperties;
import com.itheima.knowledge_platform.service.ChatSessionService;
import com.itheima.knowledge_platform.utils.SessionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.http.HttpHeaders;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ChatSessionServiceImpl implements ChatSessionService {

    @Autowired
    private FilePathProperties filePathProperties;

    @Autowired
    private ChatSessionMapper chatSessionMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserSessionRelationMapper userSessionRelationMapper;

    @Autowired
    private AIUrlProperties aiUrlProperties;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 创建新会话
     *
     * @param sessionName
     * @return
     */
    @Transactional
    @Override
    public Map<String, Object> create(String sessionName) {
        //接收前端请求，获取当前登录用户的 userId
        Long userId = BaseContext.getCurrentUserId();
        User user = userMapper.getById(userId);
        if (user == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //生成唯一的 sessionId（使用 UUID）
        String sessionId = SessionUtil.generateSessionId();
//        //历史会话存放地址
//        String historyChatPath = filePathProperties.getHistoryChatSessionPath() + sessionId + "/messages.json";
//        //根据地址创建文件
//        File file = new File(historyChatPath);
//        // 确保目录存在
//        file.getParentFile().mkdirs();

        ChatSession session = ChatSession.builder()
                .sessionId(sessionId)
                .sessionName(sessionName)
//                .jsonPath(historyChatPath)
                .build();

        //增加会话数据
        chatSessionMapper.insert(session);

        UserSessionRelation userSessionRelation = UserSessionRelation.builder()
                .userId(userId)
                .sessionId(sessionId)
                .build();
        //增加用户关联会话
        userSessionRelationMapper.insert(userSessionRelation);

        //执行完 insert 之后，session 对象里的 createdAt、updatedAt 会自动被赋值
        Map<String, Object> map = new HashMap<>();
        map.put("sessionId", session.getSessionId());
        map.put("sessionName", session.getSessionName());

        // 创建时间格式化
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        map.put("createdAt", session.getCreatedAt().format(formatter));

        return map;
    }

    /**
     * 获取我的会话列表
     *
     * @return
     */
    @Override
    public List<ChatSessionVO> list() {
        //获取当前登录用户的 userId
        Long userId = BaseContext.getCurrentUserId();
        User user = userMapper.getById(userId);
        if (user == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //查询信息
        List<ChatSessionVO> chatSessionVOS = chatSessionMapper.selectBySessionIdWithUser(user.getId());
        return chatSessionVOS;
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 发送消息
     */
    @Override
    public void sendQuery(String sessionId, String message, SseEmitter emitter) {
        Long userId = BaseContext.getCurrentUserId();
        UserSessionRelation userSessionRelation = userSessionRelationMapper.selectBySessionId(sessionId);
        if (userSessionRelation == null || !userSessionRelation.getUserId().equals(userId)) {
            throw new ChatSessionException(MessageConstant.NO_PERMISSION_TO_GET);
        }

        String token = BaseContext.getCurrentUserToken();

        new Thread(() -> {
            try {
                URL url = new URL(aiUrlProperties.getAgentBaseUrl() + "/chat/stream");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                if (token != null) {
                    connection.setRequestProperty("Authentication", token);
                }
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(300000);

                // 构建 JSON 请求体
                Map<String, String> requestBody = new HashMap<>();
                requestBody.put("session_id", sessionId);
                requestBody.put("query", message);
                String jsonBody = objectMapper.writeValueAsString(requestBody);

                try (OutputStream os = connection.getOutputStream()) {
                    os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (!line.isEmpty()) {
                            emitter.send(SseEmitter.event().data(line).name("message"));
                        }
                    }
                }
                connection.disconnect();
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();
    }

    /**
     * 重命名会话
     *
     * @param sessionId
     * @param sessionName
     */
    @Override
    public void updateName(String sessionId, String sessionName) {
        //验证会话是否存在且当前用户有权限访问
        Long userId = BaseContext.getCurrentUserId();
        UserSessionRelation userSessionRelation = userSessionRelationMapper.selectBySessionId(sessionId);
        if (userSessionRelation == null || !userSessionRelation.getUserId().equals(userId)) {
            throw new ChatSessionException(MessageConstant.NO_PERMISSION_TO_GET);
        }

        //更新 chat_session 表中的 session_name 字段
        chatSessionMapper.updateNameBySessionId(sessionId, sessionName);
    }

    /**
     * 删除会话
     */
    @Transactional
    @Override
    public void delete(String sessionId) throws IOException {
        Long userId = BaseContext.getCurrentUserId();
        UserSessionRelation userSessionRelation = userSessionRelationMapper.selectBySessionId(sessionId);
        if (userSessionRelation == null || !userSessionRelation.getUserId().equals(userId)) {
            throw new ChatSessionException(MessageConstant.NO_PERMISSION_TO_GET);
        }

        userSessionRelationMapper.deleteBySessionId(sessionId);
        chatSessionMapper.deleteBySessionId(sessionId);

//        cleanChatSession(sessionId);

        // 调用 FastAPI 删除 Agent 记忆
        try {
            restTemplate.delete(aiUrlProperties.getAgentBaseUrl() + "/memory/" + sessionId);
        } catch (Exception e) {
            log.error("删除 Agent 记忆失败, sessionId: {}", sessionId, e);
        }
    }

    //数据不放入json中
/*
    private void cleanChatSession(String sessionId) throws IOException {
        String chatDir = filePathProperties.getHistoryChatSessionPath();
        File chatFile = new File(chatDir, sessionId);
        if (chatFile.exists() && chatFile.isDirectory()) {
            FileUtils.deleteDirectory(chatFile);
        }
    }
*/

    /**
     * 获取会话历史记录
     * @param sessionId
     * @return
     */
    @Override
    public ChatSessionDetailVO showMessages(String sessionId) {
        //验证会话是否存在且当前用户有权限访问
        Long userId = BaseContext.getCurrentUserId();
        UserSessionRelation userSessionRelation = userSessionRelationMapper.selectBySessionId(sessionId);
        if (userSessionRelation == null || !userSessionRelation.getUserId().equals(userId)) {
            throw new ChatSessionException(MessageConstant.NO_PERMISSION_TO_GET);
        }

        ChatSession chatSession = chatSessionMapper.selectBySessionId(sessionId);
        //会话id是否合法
        if(chatSession==null){
            throw new ChatSessionException(MessageConstant.NO_PERMISSION_TO_GET);
        }
        ChatSessionDetailVO detailVO = ChatSessionDetailVO.builder()
                .sessionId(sessionId)
                .sessionName(chatSession.getSessionName())
                .build();

        // 调用 Python Agent 服务获取记忆
        String urlStr = aiUrlProperties.getAgentBaseUrl() + "/memory/" + sessionId;
        try {
            Map<String, Object> response = restTemplate.getForObject(urlStr, Map.class);
            List<Map<String, Object>> messagesList = (List<Map<String, Object>>) response.get("messages");

            List<ChatSessionMessage> messages = new ArrayList<>();
            if (messagesList != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                for (Map<String, Object> msgMap : messagesList) {
                    ChatSessionMessage chatMessage = new ChatSessionMessage();
                    chatMessage.setRole((String) msgMap.get("role"));
                    chatMessage.setContent((String) msgMap.get("content"));

//                    Object timestamp = msgMap.get("timestamp");
//                    if (timestamp != null) {
//                        chatMessage.setTimestamp(timestamp.toString());
//                    }
//
//                    Object refs = msgMap.get("references");
//                    if (refs != null) {
//                        chatMessage.setReferences(objectMapper.writeValueAsString(refs));
//                    }
                    messages.add(chatMessage);
                }
            }
            detailVO.setMessages(messages);
        } catch (Exception e) {
            log.error("获取会话记忆失败, sessionId: {}", sessionId, e);
            detailVO.setMessages(new ArrayList<>());
        }

        return detailVO;
    }

}
