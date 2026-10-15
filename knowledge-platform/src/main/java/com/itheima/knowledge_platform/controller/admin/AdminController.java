package com.itheima.knowledge_platform.controller.admin;

import com.itheima.knowledge_platform.pojo.dto.PlatformAdminAddDTO;
import com.itheima.knowledge_platform.pojo.dto.PlatformAdminLoginDTO;
import com.itheima.knowledge_platform.pojo.entity.PlatformAdmin;
import com.itheima.knowledge_platform.pojo.vo.PlatformAdminLoginVO;
import com.itheima.knowledge_platform.result.PageResult;
import com.itheima.knowledge_platform.result.Result;
import com.itheima.knowledge_platform.service.PlatformAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "子管理员管理")
@RequestMapping("/admin/admins")
@RestController("adminAdminController")
public class AdminController {

    @Autowired
    private PlatformAdminService platformAdminService;

    /**
     * 新增子管理员
     */

    @Operation(summary = "新增子管理员")
    @PostMapping
    public Result<PlatformAdmin> add(@RequestBody PlatformAdminAddDTO platformAdminAddDTO) {
        log.info("新增子管理员:{}", platformAdminAddDTO);
        PlatformAdmin platformAdmin = platformAdminService.add(platformAdminAddDTO);
        return Result.success(platformAdmin);
    }

    /**
     * 获取子管理员列表
     */
    @Operation(summary = "获取子管理员列表")
    @GetMapping
    public Result<PageResult<PlatformAdmin>> page(
            @RequestParam(value = "page", defaultValue = "1")
            Integer page,
            @RequestParam(value = "size", defaultValue = "10")
            Integer size,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        log.info("获取子管理员列表");
        PageResult<PlatformAdmin> result = platformAdminService.page(page, size, keyword);
        return Result.success(result);
    }

    /**
     * 启用/禁用子管理员
     */
    @Operation(summary = "启用/禁用子管理员")
    @PutMapping("/{id}/status")
    public Result<Void> setStatus(@PathVariable Long id, Integer status) {
        log.info("启用/禁用子管理员:{}", id);
        platformAdminService.setStatus(id, status);
        return Result.success();
    }

    /**
     * 删除子管理员
     */
    @Operation(summary = "删除子管理员")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除子管理员:{}", id);
        platformAdminService.delete(id);
        return Result.success();
    }

    /**
     * 重置子管理员密码
     */
    @Operation(summary = "重置子管理员密码")
    @PutMapping("/{id}/password")
    public Result<Void> updatePassword(@PathVariable Long id) {
        log.info("重置子管理员密码:{}", id);
        platformAdminService.updatePassword(id);
        return Result.success();
    }
}
