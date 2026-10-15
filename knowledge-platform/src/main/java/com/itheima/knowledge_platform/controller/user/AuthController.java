package com.itheima.knowledge_platform.controller.user;

import com.itheima.knowledge_platform.pojo.dto.UserDTO;
import com.itheima.knowledge_platform.pojo.dto.UserLoginDTO;
import com.itheima.knowledge_platform.pojo.vo.UserLoginVO;
import com.itheima.knowledge_platform.result.Result;
import com.itheima.knowledge_platform.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "用户认证模块")
@RequestMapping("/user/auth")
@RestController("userAuthController")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     *
     * @param userLoginDTO
     * @return
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {
        log.info("用户登录：{}", userLoginDTO);
        UserLoginVO userLoginVO = userService.login(userLoginDTO);
        return Result.success(userLoginVO);
    }

    /**
     * 用户注册
     *
     * @param userDTO
     * @return
     */
    //头像上传已经改进
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<?> register(@RequestBody UserDTO userDTO) {
        log.info("用户注册：{}", userDTO);
        userService.register(userDTO);
        return Result.success();
    }

    /**
     * 用户登出
     * 请求头：Authorization: Bearer {token}
     */
    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String authorization) {
        log.info("用户登出：{}", authorization);
        //userService.logout(authorization);
        return Result.success();
    }

    /**
     * 编辑用户信息
     * 账号不可换
     */
    @Operation(summary = "编辑信息")
    @PutMapping("/modify")
    public Result<Void> modify(UserDTO userDTO) {
        log.info("用户更改信息：{}", userDTO.getUsername());
        userService.modify(userDTO);
        return Result.success();
    }

    /**
     * 修改密码
     */
    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> editPassword(String oldPassword, String newPassword) {
        log.info("修改密码");
        userService.editPassword(oldPassword,newPassword);
        return Result.success();
    }
}
