package com.itheima.knowledge_platform.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "admin.comment")
public class CommentAdminProperties {
    //批量删除评论最大数目
    private Integer maxDeleteBatchCount;
}
