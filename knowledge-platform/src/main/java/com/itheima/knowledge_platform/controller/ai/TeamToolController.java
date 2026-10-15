package com.itheima.knowledge_platform.controller.ai;

import com.itheima.knowledge_platform.pojo.vo.TeamDetailVO;
import com.itheima.knowledge_platform.result.Result;
import com.itheima.knowledge_platform.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("ai/team")
@RestController
public class TeamToolController {

    @Autowired
    private TeamService teamService;

    @Operation(summary = "获取团队详情")
    @GetMapping("/getTeamMemberInfo")
    public Result<TeamDetailVO> teamDetail(Long id) {
        log.info("获取团队详情:{}", id);
        TeamDetailVO teamDetailVO = teamService.teamDetail(id);
        return Result.success(teamDetailVO);
    }
}
