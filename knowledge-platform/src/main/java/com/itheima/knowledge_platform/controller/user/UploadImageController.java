package com.itheima.knowledge_platform.controller.user;

import com.itheima.knowledge_platform.constant.MessageConstant;
import com.itheima.knowledge_platform.exception.UploadFaidedException;
import com.itheima.knowledge_platform.properties.FilePathProperties;
import com.itheima.knowledge_platform.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Tag(name = "用户上传文件")
@RequestMapping("/user/upload")
@RestController
public class UploadImageController {

    private final FilePathProperties filePathProperties;

    public UploadImageController(FilePathProperties filePathProperties) {
        this.filePathProperties = filePathProperties;
    }

    @PostMapping("/avatar")
    @Operation(summary = "头像上传")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        // 1. 校验文件
        if (file == null || file.isEmpty()) {
            log.error("上传文件为空");
            return Result.error("请选择要上传的文件");
        }

        log.info("开始上传头像：{}，大小：{} bytes", file.getOriginalFilename(), file.getSize());

        try {
            // 2. 获取配置路径（加强校验）
            String avatarPath = filePathProperties.getAvatarPath();
            if (avatarPath == null || avatarPath.isBlank()) {
                log.error("头像保存路径未配置，请检查 application.yml 中的 file.avatar-path 配置");
                throw new UploadFaidedException("文件保存路径未配置");
            }

            // 3. 规范化路径（处理斜杠问题）
            Path uploadPath = Paths.get(avatarPath).normalize();
            log.info("文件保存路径：{}", uploadPath.toAbsolutePath());

            // 4. 创建目录（如果不存在）
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("创建目录：{}", uploadPath);
            }

            // 5. 生成唯一文件名（处理文件名安全性）
            String originalFilename = file.getOriginalFilename();
            String suffix = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
                // 只允许图片格式
                String allowedExt = suffix.toLowerCase();
                if (!allowedExt.matches("\\.(jpg|jpeg|png|gif|bmp)$")) {
                    log.error("不支持的文件格式：{}", suffix);
                    return Result.error("仅支持 jpg、jpeg、png、gif、bmp 格式的图片");
                }
            }

            String fileName = UUID.randomUUID() + suffix;
            Path filePath = uploadPath.resolve(fileName);

            // 6. 保存文件
            file.transferTo(filePath.toFile());
            log.info("文件保存成功：{}", filePath);

            // 7. 返回访问地址
            String url = "/avatars/" + fileName;  // 相对路径，需要配置静态资源映射
            return Result.success(url);

        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new UploadFaidedException(MessageConstant.UPLOAD_FAILED);
        }
    }
}