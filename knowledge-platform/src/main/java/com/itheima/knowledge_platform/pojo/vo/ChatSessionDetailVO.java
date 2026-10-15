package com.itheima.knowledge_platform.pojo.vo;

import com.itheima.knowledge_platform.pojo.inner.chatsession.ChatSessionMessage;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ChatSessionDetailVO {
    private String sessionId;
    private String sessionName;
    private List<ChatSessionMessage> messages;
}
