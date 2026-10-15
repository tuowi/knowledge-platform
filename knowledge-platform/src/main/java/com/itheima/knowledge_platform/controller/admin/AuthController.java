package com.itheima.knowledge_platform.controller.admin;

import com.itheima.knowledge_platform.pojo.dto.PlatformAdminLoginDTO;
import com.itheima.knowledge_platform.pojo.vo.PlatformAdminLoginVO;
import com.itheima.knowledge_platform.result.Result;
import com.itheima.knowledge_platform.service.PlatformAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "管理员认证模块")
@RequestMapping("/admin/auth")
@RestController("adminAuthController")
public class AuthController {

    @Autowired
    private PlatformAdminService platformAdminService;

    @Operation(summary = "管理员登录")
    @PostMapping("/login")
    public Result<PlatformAdminLoginVO> login(@RequestBody PlatformAdminLoginDTO platformAdminLoginDTO){
        log.info("管理员登录：{}，{}",platformAdminLoginDTO.getAdminAccount(),platformAdminLoginDTO.getAdminPwd());
        PlatformAdminLoginVO platformAdminLoginVO=platformAdminService.login(platformAdminLoginDTO);
        return Result.success(platformAdminLoginVO);
    }

    /**
     * 管理员登出
     */
    @Operation(summary = "管理员登出")
    @PostMapping("/logout")
    public Result<?> logout(){
        return Result.success();
    }
}
