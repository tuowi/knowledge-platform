package com.itheima.knowledge_platform.controller.ai;

import com.itheima.knowledge_platform.constant.MessageConstant;
import com.itheima.knowledge_platform.context.BaseContext;
import com.itheima.knowledge_platform.enums.DocumentAuditStatusEnum;
import com.itheima.knowledge_platform.enums.DocumentStatusEnum;
import com.itheima.knowledge_platform.enums.DocumentVisibilityEnum;
import com.itheima.knowledge_platform.exception.DocumentErrorException;
import com.itheima.knowledge_platform.mapper.DocumentMapper;
import com.itheima.knowledge_platform.mapper.TeamMapper;
import com.itheima.knowledge_platform.mapper.TeamMemberMapper;
import com.itheima.knowledge_platform.mapper.UserMapper;
import com.itheima.knowledge_platform.pojo.entity.Document;
import com.itheima.knowledge_platform.pojo.entity.Team;
import com.itheima.knowledge_platform.pojo.entity.TeamMember;
import com.itheima.knowledge_platform.pojo.entity.User;
import com.itheima.knowledge_platform.pojo.vo.DocumentAIVO;
import com.itheima.knowledge_platform.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/ai/document")
@RestController
public class DocumentToolController {

    @Autowired
    private DocumentMapper documentMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private TeamMemberMapper teamMemberMapper;
    @Autowired
    private TeamMapper teamMapper;

    @GetMapping("/getDocumentInfo")
    public Result<DocumentAIVO> list(Long id) {
        log.info("获取文档详细");
        Long userId = BaseContext.getCurrentUserId();
        User user = userMapper.getById(userId);
        if (user == null) {
            throw new DocumentErrorException(MessageConstant.NO_PERMISSION_TO_READ_DOCUMENT);
        }

        Document result = documentMapper.getById(id);
        if (result==null){
            throw new DocumentErrorException(MessageConstant.DOCUMENT_NOT_FOUND);
        }
        Team team = teamMapper.selectById(result.getTeamId());
        User author = userMapper.getById(result.getAuthorId());

        DocumentAIVO documentAIVO = new DocumentAIVO();
        BeanUtils.copyProperties(result, documentAIVO);

        documentAIVO.setAuthorName(author.getUsername());
        documentAIVO.setTeamName(team.getName());
        documentAIVO.setStatusName(DocumentStatusEnum.getByCode(result.getStatus()).getMessage());
        documentAIVO.setAuditStatusName(DocumentAuditStatusEnum.getByCode(result.getAuditStatus()).getMessage());
        documentAIVO.setVisibilityName(DocumentVisibilityEnum.getByCode(result.getVisibility()).getMessage());

        if (result.getVisibility().equals(DocumentVisibilityEnum.PRIVATE.getCode()) && result.getAuthorId().equals(userId)) {
            return Result.success(documentAIVO);
        }

        if (result.getVisibility().equals(DocumentVisibilityEnum.WITHIN_THE_TEAM.getCode())) {
            TeamMember teamMember = teamMemberMapper.selectByTeamIdAndUserId(result.getTeamId(), userId);

            if (teamMember == null) {
                throw new DocumentErrorException(MessageConstant.NO_PERMISSION_TO_READ_DOCUMENT);
            }

            return Result.success(documentAIVO);
        }

        if (result.getAuditStatus().equals(DocumentAuditStatusEnum.APPROVED.getCode())) {
            return Result.success(documentAIVO);
        }
        return Result.success();
    }
}
