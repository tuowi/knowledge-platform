package com.itheima.knowledge_platform.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "file")
@Data
public class FilePathProperties {

    //头像上传地址
    private String avatarPath;

    //分块文档地址
    private String tempPath;

    //合并后文档所在地址
    private String mergePath;

    //历史版本存放地址
    private String historyPath;

    //存到文档正式存放地址
    private String formalPath;

    //存放会话历史记录
    private String historyChatSessionPath;
}
