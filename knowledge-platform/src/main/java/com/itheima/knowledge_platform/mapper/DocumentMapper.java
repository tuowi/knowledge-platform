package com.itheima.knowledge_platform.mapper;

import com.github.pagehelper.Page;
import com.itheima.knowledge_platform.annotation.AutoFill;
import com.itheima.knowledge_platform.enums.TimeOperationEnum;
import com.itheima.knowledge_platform.pojo.dto.DocumentAdminDetailPageDTO;
import com.itheima.knowledge_platform.pojo.dto.DocumentQueryPageDTO;
import com.itheima.knowledge_platform.pojo.entity.Document;
import com.itheima.knowledge_platform.pojo.inner.document.*;
import com.itheima.knowledge_platform.pojo.inner.user.UserStatistics;
import com.itheima.knowledge_platform.pojo.vo.DocumentPageVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DocumentMapper {

    /**
     * 根据id查询文档
     * @param id
     * @return
     */
    @Select("select * from document where id=#{id}")
    Document getById(Long id);

    /**
     * 根据id动态更新文档（除了内容和版本号）
     * 版本号乐观锁---用户不可修改，后台自动更新
     * @param document
     */
    @AutoFill(TimeOperationEnum.UPDATE)
    void updateById(Document document);

    /**
     * 仅更新文档内容
     * 后台自动更新乐观锁
     * @param document
     */
    @AutoFill(TimeOperationEnum.UPDATE)
    @Update("update document set content=#{content},version=version+1,updated_at=#{updatedAt} where id=#{id} and version=#{version}")
    Integer updateContentById(Document document);

    @AutoFill(TimeOperationEnum.INSERT)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id") //主键回显
    @Insert("insert into document (title, category_id, team_id, status, visibility, author_id, created_at, updated_at)\n" +
            "VALUES (#{title},#{categoryId},#{teamId},#{status},#{visibility},#{authorId},#{createdAt},#{updatedAt})")
    void insert(Document document);

    /**
     * 删除文档根据id
     * @param id
     */
    @Delete("delete from document where id=#{id}")
    void deleteById(Long id);

    /**
     * 分页查询
     * @param documentQueryPageDTO
     * @return
     */
    Page<DocumentPageVO> pageQuery(DocumentQueryPageDTO documentQueryPageDTO);

    /**
     * 增加点赞数
     * @param id
     */
    @Update("update document set like_count=like_count+1 where id=#{id}")
    void incLikeCount(Long id);

    /**
     * 减少点赞
     * @param id
     */
    @Update("update document set like_count=like_count-1 where id=#{id}")
    void decLikeCount(Long id);

    /**
     * 增加收藏数
     * @param id
     */
    @Update("update document set favorite_count=favorite_count+1 where id=#{id}")
    void incFavoriteCount(Long id);

    /**
     * 取消收藏
     * @param id
     */
    @Update("update document set favorite_count=favorite_count-1 where id=#{id}")
    void decFavoriteCount(Long id);

    /**
     * 根据分类id查询
     * @param categoryId
     * @return
     */
    @Select("select * from document where category_id=#{categoryId}")
    List<Document> selectByCategoryId(Long categoryId);

    /**
     * 查询该用户在该团队下有多少文档
     * @param authorId
     * @param teamId
     * @return
     */
    @Select("select * from document where author_id=#{authorId} and team_id=#{teamId}")
    List<Document> selectByAuthorIdAndTeamId(Long authorId, Long teamId);

    @Select("select * from document where team_id=#{teamId}")
    List<Document> selectByTeamId(Long teamId);

    List<Document> selectByAuditStatusAndTitle(Integer auditStatus, String title);

    List<Document> selectByDocumentAdminDetailPageDTO(DocumentAdminDetailPageDTO documentAdminDetailPageDTO);

    /**
     * 更新审核状态------需要审核状态乐观锁
     * @param document
     */
    @AutoFill(TimeOperationEnum.UPDATE)
    Integer updateReviewById(Document document);

    UserStatistics statisticsWithUser(Long authorId);

    /**
     * 根据作者id统计文档信息
     * @param authorId
     * @return
     */
    Summary summaryUser(Long authorId);

    /**
     * 按状态分组：草稿、已发布、已归档
     * @param authorId
     * @return
     */
    ByStatus byStatusUser(Long authorId);

    /**
     * 按审核状态分组：待审核、已通过、已驳回
     * @param authorId
     * @return
     */
    ByAuditStatus byAuditStatusUser(Long authorId);

    /**
     * 按可见性分组：私有、团队内、公开
     * @param authorId
     * @return
     */
    ByVisibility byVisibilityUser(Long authorId);
}
