package com.itheima.knowledge_platform.service;

import com.itheima.knowledge_platform.pojo.dto.*;
import com.itheima.knowledge_platform.pojo.entity.Document;
import com.itheima.knowledge_platform.pojo.vo.*;
import com.itheima.knowledge_platform.result.PageResult;
import jakarta.validation.Valid;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

public interface DocumentService {

    /**
     * 更新文档表的content字段
     * @param documentId
     * @param finalPath
     */
    //void updateContentPath(Long documentId, String finalPath);

    /**
     * 创建文档
     * @param documentDTO
     * @return
     */
    DocumentVO save(DocumentDTO documentDTO);

    /**
     * 文档关联接口
     * @param id
     * @param content
     * @return
     */
    DocumentConnectVO connect(Long id, String content) throws IOException;

    /**
     * 更新文档元数据
     * @param id
     * @param documentMetadataDTO
     */
    void editMetadata(Long id, DocumentMetadataDTO documentMetadataDTO);

    /**
     * 删除文档
     * @param id
     */
    void deleteDocument(Long id);

    /**
     * 文档列表分页查询
     * @param documentQueryPageDTO
     * @return
     */
    PageResult<DocumentPageVO> page(DocumentQueryPageDTO documentQueryPageDTO);

    /**
     * 发布文档
     * @param id
     */
    void publish(Long id);

    /**
     * 归档文档
     * @param id
     */
    void archive(Long id);

    /**
     * 点赞/取消点赞文档
     * @param id
     */
    void like(Long id);

    /**
     * 获取文档历史版本列表
     * @param id
     * @return
     */
    List<DocumentVersionListPageVO> version(Long id);

    /**
     * 版本回滚
     * @param id
     * @param version
     */
    void rollback(Long id, Integer version) throws IOException;

    /**
     *  获取待审核文档列表
     * @param page
     * @param size
     * @param title
     * @return
     */
    PageResult<DocumentAdminVO> adminReviewPage(Integer page, Integer size, String title);

    /**
     * 获取所有文档列表
     * @param documentAdminDetailPageDTO
     * @return
     */
    PageResult<DocumentAdminDetailPageVO> adminPage(@Valid DocumentAdminDetailPageDTO documentAdminDetailPageDTO);

    /**
     * 审核文档
     * @param id
     * @param documentReviewAdminDTO
     * @return
     */
    DocumentReviewAdminVO adminReview(Long id, @Valid DocumentReviewAdminDTO documentReviewAdminDTO);

    /**
     * 用户是否用权限读取文献
     * @param id
     * @param userId
     * @return
     */
    Boolean hasReadPermission(Long id, Long userId);

    /**
     * 异步执行流式输出（
     * @param documentId
     * @param startLine
     * @param linesPerBatch
     * @param speed
     * @param emitter
     */
    void streamContentByLine(Long documentId, Long startLine, Integer linesPerBatch, String speed, SseEmitter emitter);

    /**
     * 流式输出文档内容（按字节流，处理UTF-8边界）
     * 适用于超大文件，支持断点续传
     * @param documentId
     * @param startLine
     * @param chunkSize
     * @param speed
     * @param emitter
     */
    void streamContentByByte(Long documentId, Long startLine, Integer chunkSize, String speed, SseEmitter emitter);
}
