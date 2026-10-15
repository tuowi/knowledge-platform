package com.itheima.knowledge_platform.service.Impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.itheima.knowledge_platform.constant.MessageConstant;
import com.itheima.knowledge_platform.context.BaseContext;
import com.itheima.knowledge_platform.exception.CommentErrorException;
import com.itheima.knowledge_platform.exception.DocumentNotFoundException;
import com.itheima.knowledge_platform.mapper.CommentMapper;
import com.itheima.knowledge_platform.mapper.DocumentMapper;
import com.itheima.knowledge_platform.mapper.PlatformAdminMapper;
import com.itheima.knowledge_platform.mapper.UserMapper;
import com.itheima.knowledge_platform.pojo.dto.CommentDTO;
import com.itheima.knowledge_platform.pojo.dto.CommentQueryRequestDTO;
import com.itheima.knowledge_platform.pojo.entity.Comment;
import com.itheima.knowledge_platform.pojo.entity.Document;
import com.itheima.knowledge_platform.pojo.entity.PlatformAdmin;
import com.itheima.knowledge_platform.pojo.entity.User;
import com.itheima.knowledge_platform.pojo.vo.CommentAdminVO;
import com.itheima.knowledge_platform.pojo.vo.CommentVO;
import com.itheima.knowledge_platform.properties.CommentAdminProperties;
import com.itheima.knowledge_platform.result.PageResult;
import com.itheima.knowledge_platform.service.CommentService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private DocumentMapper documentMapper;

    @Autowired
    private PlatformAdminMapper platformAdminMapper;

    @Autowired
    private CommentAdminProperties commentAdminProperties;

    @Autowired
    private UserMapper userMapper;

    /**
     * 发表评论
     *
     * @param commentDTO
     * @return
     */
    @Override
    public Long publish(CommentDTO commentDTO) {
        //校验文档是否存在
        Document document = documentMapper.getById(commentDTO.getDocumentId());

        if (document == null) {
            throw new DocumentNotFoundException("[publish]" + MessageConstant.DOCUMENT_NOT_FOUND);
        }

        //校验父评论（parentId≠0 时）是否存在
/*        Comment comment = commentMapper.selectById(commentDTO.getParentId());

        if (comment == null) {
            throw new CommentErrorException(MessageConstant.PARENT_COMMENT_NOT_FOUND);
        }*/

        //校验评论内容非空
        if (commentDTO.getContent() == null || commentDTO.getContent().isEmpty()) {
            throw new CommentErrorException(MessageConstant.COMMENT_CONTENT_NOT_NULL);
        }

        //插入评论记录到 comment 表
        Comment comment1 = new Comment();
        BeanUtils.copyProperties(commentDTO, comment1);
        comment1.setUserId(BaseContext.getCurrentUserId());
        comment1.setCreatedAt(LocalDateTime.now());
        commentMapper.insert(comment1);

        //TODO 处理提及用户 mentionedUsers（插入 @用户关联表，没有就暂时不处理）

        //返回成功 + 评论 ID
        return comment1.getId();
    }

    /**
     * 删除评论
     *
     * @param id
     */
    @Transactional
    @Override
    public void delete(Long id) {
        //校验评论是否存在
        Comment comment = commentMapper.selectById(id);

        if (comment == null) {
            throw new CommentErrorException(MessageConstant.COMMENT_NOT_FOUND);
        }

        //校验当前用户是否是评论作者 或 管理员
        if (!BaseContext.getCurrentId().equals(comment.getUserId())) {
            throw new CommentErrorException(MessageConstant.NO_PERMISSION_DELETE_COMMENT);
        }

        //删除该评论
        commentMapper.deleteById(comment.getId());

        //如果是一级评论，同时删除它的所有回复
        commentMapper.deleteByParentId(comment.getId());
    }


    /**
     * 删除评论
     *
     * @param id
     */
    @Transactional
    @Override
    public void deleteAdmin(Long id) {
        //校验评论是否存在
        Comment comment = commentMapper.selectById(id);

        if (comment == null) {
            throw new CommentErrorException(MessageConstant.COMMENT_NOT_FOUND);
        }

        //校验当前用户是否是 管理员
        Long currentAdminId = BaseContext.getCurrentAdminId();

        PlatformAdmin platformAdmin = platformAdminMapper.selectById(currentAdminId);
        if (platformAdmin == null) {
            throw new CommentErrorException(MessageConstant.NO_PERMISSION_DELETE_COMMENT);
        }

        //删除该评论
        commentMapper.deleteById(comment.getId());

        //如果是一级评论，同时删除它的所有回复
        commentMapper.deleteByParentId(comment.getId());
    }

    /**
     * 批量删除评论
     *
     * @param commentIds
     */
    @Transactional
    @Override
    public void deleteAdminBatch(List<Long> commentIds) {
        //commentIds不能为空
        if (commentIds.isEmpty()) {
            throw new CommentErrorException(MessageConstant.DELETE_COMMENT_NOT_NULL);
        }

        //列表大小不能超过50（防止恶意请求）
        if (commentIds.size() > commentAdminProperties.getMaxDeleteBatchCount()) {
            throw new CommentErrorException(MessageConstant.DELETE_COMMENT_COUNT_OVER);
        }

        //去重：对commentIds进行去重
        List<Long> distinctIds = commentIds.stream()
                .distinct()
                .collect(Collectors.toList());

        //校验评论是否存在
        for (Long id : distinctIds) {
            Comment comment = commentMapper.selectById(id);

            if (comment == null) {
                throw new CommentErrorException(MessageConstant.COMMENT_NOT_FOUND);
            }
        }

        //校验当前用户是否是 管理员
        Long currentAdminId = BaseContext.getCurrentAdminId();

        PlatformAdmin platformAdmin = platformAdminMapper.selectById(currentAdminId);
        if (platformAdmin == null) {
            throw new CommentErrorException(MessageConstant.NO_PERMISSION_DELETE_COMMENT);
        }

        //级联删除
        commentMapper.deleteBatchWithChildren(distinctIds);
    }

    /**
     * 获取所有评论列表
     *
     * @param commentQueryRequestDTO
     * @return
     */
    @Override
    public PageResult<CommentAdminVO> queryAdminPage(CommentQueryRequestDTO commentQueryRequestDTO) {
        // 1. 参数校验
        if (commentQueryRequestDTO.getPage() < 1)
            commentQueryRequestDTO.setPage(1);
        if (commentQueryRequestDTO.getSize() < 1)
            commentQueryRequestDTO.setSize(10);
        if (commentQueryRequestDTO.getSize() > 100)
            commentQueryRequestDTO.setSize(100);

        //开始分页
        PageHelper.startPage(commentQueryRequestDTO.getPage(), commentQueryRequestDTO.getSize());

        //查询数据库
        List<CommentAdminVO> comments = commentMapper.selectByCommentQueryRequestDTO(commentQueryRequestDTO);

        Page<CommentAdminVO> commentsPage = (Page<CommentAdminVO>) comments;

        return new PageResult<>(commentsPage.getTotal(), commentsPage.getPageNum(), commentsPage.getPageSize(), comments);
    }

    /**
     * 获取文档评论列表
     *
     * @param documentId
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageResult<CommentVO> getCommentListByDocument(Long documentId, Integer page, Integer size) {
        // 1. 分页查询顶级评论
        PageHelper.startPage(page, size);
        List<Comment> comments = commentMapper.selectTopByDocumentId(documentId);
        PageInfo<Comment> pageInfo = new PageInfo<>(comments);

        // 2. 转换为 VO 并加载回复
        List<CommentVO> commentVOS = new ArrayList<>();
        for (Comment comment : comments) {
            CommentVO vo = convertToVO(comment);
            // 查询该评论的回复
            List<Comment> replies = commentMapper.selectRepliesByParentId(comment.getId());
            vo.setReplies(replies.stream().map(this::convertToVO).collect(Collectors.toList()));
            commentVOS.add(vo);
        }

        // 3. 返回分页结果
        return new PageResult<>(pageInfo.getTotal(), page, size, commentVOS);
    }

    /**
     * 转换实体到VO
     */
    private CommentVO convertToVO(Comment comment) {
        User user = userMapper.getById(comment.getUserId());

        return CommentVO.builder()
                .id(comment.getId())
                .userId(comment.getUserId())
                .username(user.getUsername())
                .userAvatar(user.getAvatar())
                .parentId(comment.getParentId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .replies(new ArrayList<>())
                .build();
    }
}
