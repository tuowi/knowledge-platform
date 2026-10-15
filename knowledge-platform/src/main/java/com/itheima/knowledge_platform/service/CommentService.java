package com.itheima.knowledge_platform.service;

import com.itheima.knowledge_platform.pojo.dto.CommentDTO;
import com.itheima.knowledge_platform.pojo.dto.CommentQueryRequestDTO;
import com.itheima.knowledge_platform.pojo.vo.CommentAdminVO;
import com.itheima.knowledge_platform.pojo.vo.CommentVO;
import com.itheima.knowledge_platform.result.PageResult;
import jakarta.validation.Valid;

import java.util.List;

public interface CommentService {

    /**
     * 发表评论
     * @param commentDTO
     * @return
     */
    Long publish(CommentDTO commentDTO);

    /**
     * 删除评论
     * @param id
     */
    void delete(Long id);

    /**
     * 删除评论
     * @param id
     */
    void deleteAdmin(Long id);

    /**
     * 批量删除评论
     * @param commentIds
     */
    void deleteAdminBatch(List<Long> commentIds);

    /**
     *  获取所有评论列表
     * @param commentQueryRequestDTO
     * @return
     */
    PageResult<CommentAdminVO> queryAdminPage(@Valid CommentQueryRequestDTO commentQueryRequestDTO);

    /**
     * 获取文档评论列表
     * @param documentId
     * @param page
     * @param size
     * @return
     */
    PageResult<CommentVO> getCommentListByDocument(Long documentId, Integer page, Integer size);
}
