package com.itheima.knowledge_platform.mapper;

import com.itheima.knowledge_platform.pojo.entity.UploadSession;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UploadSessionMapper {

    @Insert("INSERT INTO upload_session (upload_id, document_id, total_chunks, uploaded_chunks, status) " +
            "VALUES (#{uploadId}, #{documentId}, #{totalChunks}, #{uploadedChunks}, #{status})")
    int insert(UploadSession session);

    @Select("SELECT * FROM upload_session WHERE upload_id = #{uploadId}")
    UploadSession selectByUploadId(@Param("uploadId") String uploadId);

    @Update("UPDATE upload_session SET uploaded_chunks = #{uploadedChunks}, status = #{status} " +
            "WHERE upload_id = #{uploadId}")
    int update(UploadSession session);

    @Update("UPDATE upload_session SET status = #{status} WHERE upload_id = #{uploadId}")
    int updateStatus(@Param("uploadId") String uploadId, @Param("status") String status);

    @Delete("DELETE FROM upload_session WHERE upload_id = #{uploadId}")
    int deleteByUploadId(@Param("uploadId") String uploadId);

    // 清理过期的会话（比如超过24小时未完成）
    @Delete("DELETE FROM upload_session WHERE status = 'uploading' AND created_at < DATE_SUB(NOW(), INTERVAL 24 HOUR)")
    int deleteExpiredSessions();

    @Select("SELECT upload_id FROM upload_session WHERE status = 'uploading' AND created_at < DATE_SUB(NOW(), INTERVAL 24 HOUR)")
    List<String> selectExpiredUploadIds();
}
