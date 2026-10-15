package com.itheima.knowledge_platform.mapper;

import com.itheima.knowledge_platform.pojo.dto.CommentQueryRequestDTO;
import com.itheima.knowledge_platform.pojo.entity.Comment;
import com.itheima.knowledge_platform.pojo.vo.CommentAdminVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CommentMapper {

    /**
     * 根据id查询
     * @param id
     * @return
     */
    @Select("select * from comment where id=#{id}")
    Comment selectById(Long id);

    /**
     * 添加数据
     * @param comment
     */
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    @Insert("insert into comment (document_id, parent_id, user_id, content, created_at) " +
            "VALUES (#{documentId},#{parentId},#{userId},#{content},#{createdAt})")
    void insert(Comment comment);

    /**
     * 根据id删除
     * @param id
     */
    @Delete("delete from comment where id=#{id}")
    void deleteById(Long id);

    /**
     * 跟随父评论一起删除
     * @param parentId
     */
    @Delete("delete from comment where parent_id=#{parentId}")
    void deleteByParentId(Long parentId);

    void deleteBatchWithChildren(List<Long> ids);

    List<CommentAdminVO> selectByCommentQueryRequestDTO(CommentQueryRequestDTO commentQueryRequestDTO);

    /**
     * 查询文档的顶级评论（分页）
     */
    List<Comment> selectTopByDocumentId(@Param("documentId") Long documentId);

    /**
     * 根据父评论ID查询回复
     */
    List<Comment> selectRepliesByParentId(@Param("parentId") Long parentId);
}
