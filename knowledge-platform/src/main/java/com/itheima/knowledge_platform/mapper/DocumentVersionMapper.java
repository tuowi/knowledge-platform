package com.itheima.knowledge_platform.mapper;

import com.itheima.knowledge_platform.pojo.entity.DocumentVersion;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DocumentVersionMapper {

    /**
     * 添加版本信息
     * @param documentVersion
     */
    @Insert("insert into document_version (document_id, version, editor_id, created_at)\n" +
            "VALUES (#{documentId},#{version},#{editorId},#{createdAt})")
    void insert(DocumentVersion documentVersion);

    /**
     * 根据文档id删除信息
     * @param documentId
     */
    @Delete("delete from document_version where document_id=#{documentId}")
    void deleteByDocumentId(@Param("documentId") Long documentId);

    /**
     * 根据文档id查询文本历史版本列表
     * @param documentId
     * @return
     */
    @Select("select * from document_version where document_id=#{documentId}")
    List<DocumentVersion> selectByDocumentId(Long documentId);

    /**
     * 根据文档id和历史版本找寻版本内容
     * @param id
     * @param version
     * @return
     */
    @Select("select * from document_version where document_id=#{documentId} and version=#{version}")
    DocumentVersion selectByDocumentIdAndVersion(Long id, Integer version);
}
