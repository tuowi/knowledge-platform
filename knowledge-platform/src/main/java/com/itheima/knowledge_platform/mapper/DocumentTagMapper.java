package com.itheima.knowledge_platform.mapper;

import com.itheima.knowledge_platform.pojo.entity.DocumentTag;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DocumentTagMapper {

    /**
     * 添加文档所属标签
     * @param documentTag
     */
    @Insert("insert into document_tag (document_id, tag_id) VALUES (#{documentId},#{tagId})")
    void insert(DocumentTag documentTag);

    /**
     * 根据文档id修改文档标签
     * @param tagId
     * @param documentId
     */
    @Update("update document_tag set tag_id=#{tagId} where document_id=#{documentId}")
    void updateById(Long tagId, Long documentId);

    /**
     * 根据文档id删除相关标签
     * @param documentId
     */
    @Delete("delete from document_tag where document_id=#{documentId}")
    void deleteByDocumentId(@Param("documentId") Long documentId);

    /**
     * 根据标签id删除
     * @param id
     */
    @Delete("delete from document_tag where tag_id=#{tagId}")
    void deleteByTagsId(Long id);

    @Select("select * from document_tag where tag_id=#{tagId}")
    List<DocumentTag> selectByTagId(Long tagId);
}
