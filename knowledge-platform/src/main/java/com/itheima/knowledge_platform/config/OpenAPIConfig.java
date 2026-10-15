package com.itheima.knowledge_platform.config;

import com.itheima.knowledge_platform.properties.JwtProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Autowired
    private JwtProperties jwtProperties;

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("管理端接口")
                .packagesToScan("com.itheima.knowledge_platform.controller.admin")
                // 关键：为每个接口添加安全要求
                .addOperationCustomizer((operation, handlerMethod) -> {
                    operation.addSecurityItem(new SecurityRequirement()
                            .addList(jwtProperties.getAdminTokenName()));
                    return operation;
                })
                .build();
    }

    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("用户端接口")
                .packagesToScan("com.itheima.knowledge_platform.controller.user")
                // 关键：为每个接口添加安全要求
                .addOperationCustomizer((operation, handlerMethod) -> {
                    operation.addSecurityItem(new SecurityRequirement()
                            .addList(jwtProperties.getUserTokenName()));
                    return operation;
                })
                .build();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        String userTokenName = jwtProperties.getUserTokenName();
        String adminTokenName = jwtProperties.getAdminTokenName();

        return new OpenAPI()
                .info(new Info()
                        .title("智能企业知识库平台接口文档")
                        .version("3.0")
                        .description("智能企业知识库项目接口文档"))
                .components(new Components()
                        .addSecuritySchemes(userTokenName, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name(userTokenName))
                        .addSecuritySchemes(adminTokenName, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name(adminTokenName))
                )
                // 全局添加安全要求
                .addSecurityItem(new SecurityRequirement()
                        .addList(userTokenName)
                        .addList(adminTokenName));
    }
}