package com.itheima.knowledge_platform.controller.admin;

import com.itheima.knowledge_platform.pojo.dto.TeamQueryRequestDTO;
import com.itheima.knowledge_platform.pojo.vo.TeamAdminDetailVO;
import com.itheima.knowledge_platform.pojo.vo.TeamAdminVO;
import com.itheima.knowledge_platform.pojo.vo.TeamDetailVO;
import com.itheima.knowledge_platform.result.PageResult;
import com.itheima.knowledge_platform.result.Result;
import com.itheima.knowledge_platform.service.TeamMemberService;
import com.itheima.knowledge_platform.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "团队管控模块")
@RequestMapping("/admin/teams")
@RestController("adminTeamController")
public class TeamController {

    @Autowired
    private TeamService teamService;
    @Autowired
    private TeamMemberService teamMemberService;

    /**
     * 获取所有团队列表
     *
     * @param teamQueryRequestDTO
     * @return
     */
    @Operation(summary = "获取所有团队列表")
    @GetMapping
    public Result<PageResult<TeamAdminVO>> page(@Valid TeamQueryRequestDTO teamQueryRequestDTO) {
        log.info("获取所有团队列表");
        PageResult<TeamAdminVO> result = teamService.pageAdmin(teamQueryRequestDTO);
        return Result.success(result);
    }

    /**
     * 获取团队详情
     */
    @Operation(summary = "获取团队详情")
    @GetMapping("/{id}")
    public Result<TeamAdminDetailVO> teamDetailAdmin(@PathVariable Long id) {
        log.info("获取团队详情：{}", id);
        TeamAdminDetailVO teamAdminDetailVO = teamService.teamDetailAdmin(id);
        return Result.success(teamAdminDetailVO);
    }

    /**
     * 强制解散团队
     */
    @Operation(summary = "强制解散团队")
    @DeleteMapping("/{id}")
    public Result<Void> disbandAdmin(@PathVariable Long id){
        log.info("强制解散团队:{}",id);
        teamMemberService.disbandAdmin(id);
        return Result.success();
    }
}
