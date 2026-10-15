package com.itheima.knowledge_platform.controller.user;

import com.itheima.knowledge_platform.pojo.vo.ChatSessionDetailVO;
import com.itheima.knowledge_platform.pojo.vo.ChatSessionVO;
import com.itheima.knowledge_platform.result.Result;
import com.itheima.knowledge_platform.service.ChatSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

@Slf4j
@Tag(name = "AI 智能助手模块")
@RequestMapping("/user/ai/sessions")
@RestController("userAIController")
public class AIController {

    @Autowired
    private ChatSessionService chatSessionService;

    /**
     * 创建新会话
     *
     * @param sessionName
     * @return
     */
    @Operation(summary = "创建新会话")
    @PostMapping
    public Result<?> create(String sessionName) {
        log.info("创建新会话:{}", sessionName);
        Map<String, Object> map = new HashMap<>();
        map = chatSessionService.create(sessionName);
        return Result.success(map);
    }

    /**
     * 获取我的会话列表
     */
    @Operation(summary = "获取我的会话列表")
    @GetMapping
    public Result<List<ChatSessionVO>> list() {
        log.info("获取我的会话列表");
        List<ChatSessionVO> result = chatSessionService.list();
        return Result.success(result);
    }

    /**
     * 发送消息（AI问答）
     */
    @Operation(summary = "发送消息（AI问答）")
    @PostMapping(value = "/{sessionId}/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sendQuery(
            @PathVariable String sessionId,
            @RequestBody String message,
            HttpServletResponse response
    ) {
        log.info("发送消息（AI问答）:{},用户消息：{}", sessionId, message);

        // 1. 禁用 Nginx 缓冲（SSE 必须）
        response.setHeader("X-Accel-Buffering", "no");

        // 2. 创建 SSE 发射器（超时 5 分钟）
        SseEmitter emitter = new SseEmitter(300000L);

        try {
            chatSessionService.sendQuery(sessionId, message, emitter);
        } catch (Exception e) {
            log.error("发送消息异常：{}", e.getMessage());
            emitter.completeWithError(e);
        }
        return emitter;
    }

    /**
     * 获取会话历史记录
     */
    @Operation(summary = "获取会话历史记录")
    @GetMapping("/{sessionId}/messages")
    public Result<ChatSessionDetailVO> messages(@PathVariable String sessionId) {
        log.info("获取会话历史记录:{}", sessionId);
        ChatSessionDetailVO result = chatSessionService.showMessages(sessionId);
        return Result.success(result);
    }

    /**
     * 删除会话
     */
    @Operation(summary = "删除会话")
    @DeleteMapping("/{sessionId}")
    public Result<Void> delete(@PathVariable String sessionId) throws IOException {
        log.info("删除会话：{}", sessionId);
        chatSessionService.delete(sessionId);
        return Result.success();
    }

    /**
     * 重命名会话
     */
    @Operation(summary = "重命名会话")
    @PutMapping("/{sessionId}/name")
    public Result<Void> updateName(@PathVariable String sessionId, String sessionName) {
        log.info("重命名会话：{}，{}", sessionId, sessionName);
        chatSessionService.updateName(sessionId, sessionName);
        return Result.success();
    }
}
