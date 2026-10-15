package com.itheima.knowledge_platform.controller.user;

import com.itheima.knowledge_platform.pojo.vo.FavoriteDocumentVO;
import com.itheima.knowledge_platform.result.PageResult;
import com.itheima.knowledge_platform.result.Result;
import com.itheima.knowledge_platform.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "收藏（点赞）管理模块")
@RequestMapping("/user/favorites")
@RestController
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    /**
     * 收藏文档
     */
    @Operation(summary = "收藏文档")
    @PostMapping
    public Result<Void> favorite(Long documentId){
        log.info("收藏文档：{}",documentId);
        favoriteService.favorite(documentId);
        return Result.success();
    }

    /**
     * 取消收藏
     */
    @Operation(summary = "取消收藏")
    @DeleteMapping("/{documentId}")
    public Result<Void> cancel(@PathVariable Long documentId){
        log.info("取消收藏：{}",documentId);
        favoriteService.cancel(documentId);
        return Result.success();
    }

    /**
     * 获取我的收藏列表
     */
    @Operation(summary = "获取我的收藏列表")
    @GetMapping
    public Result<PageResult<FavoriteDocumentVO>> page(
            @RequestParam(value = "page",defaultValue = "1")
            Integer page,
            @RequestParam(value = "size",defaultValue = "10")
            Integer size
    ) {
        log.info("获取我的收藏文档");
        PageResult<FavoriteDocumentVO> list = favoriteService.page(page, size);
        return Result.success(list);
    }
}
