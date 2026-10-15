package com.itheima.knowledge_platform.mapper;

import com.itheima.knowledge_platform.pojo.entity.Like;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface LikeMapper {

    /**
     * 通过文档ID和用户ID查询点赞信息
     * @param documentId
     * @param userId
     * @return
     */
    @Select("select * from `like` where document_id =#{documentId} and user_id=#{userId}")
    Like getByDocumentIdAndUserId(Long documentId, Long userId);

    /**
     * 添加点赞文档
     * @param like
     */
    @Insert("insert into `like` (user_id, document_id, created_at) VALUES (#{userId},#{documentId},#{createdAt})")
    void insert(Like like);

    /**
     * 根据id删除点赞记录
     * @param id
     */
    @Delete("delete from `like` where id=#{id}")
    void deleteById(Long id);
}
