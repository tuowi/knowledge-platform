package com.itheima.knowledge_platform.mapper;

import com.github.pagehelper.Page;
import com.itheima.knowledge_platform.pojo.entity.Favorite;
import com.itheima.knowledge_platform.pojo.vo.FavoriteDocumentVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FavoriteMapper {

    /**
     * 通过文档ID和用户ID查询收藏信息
     * @param documentId
     * @param userId
     * @return
     */
    @Select("select * from favorite where document_id =#{documentId} and user_id=#{userId}")
    Favorite getByDocumentIdAndUserId(Long documentId, Long userId);

    /**
     * 添加收藏文档
     * @param favorite
     */
    @Insert("insert into favorite (user_id, document_id, created_at) VALUES (#{userId},#{documentId},#{createdAt})")
    void insert(Favorite favorite);

    /**
     * 根据id删除收藏记录
     * @param id
     */
    @Delete("delete from favorite where id=#{id}")
    void deleteById(Long id);

    /**
     * 分页查询
     * @return
     */
    Page<FavoriteDocumentVO> pageQuery(Long userId);
}
