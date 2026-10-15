package com.itheima.knowledge_platform.service.Impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itheima.knowledge_platform.constant.MessageConstant;
import com.itheima.knowledge_platform.constant.StatusConstant;
import com.itheima.knowledge_platform.context.BaseContext;
import com.itheima.knowledge_platform.enums.*;
import com.itheima.knowledge_platform.exception.*;
import com.itheima.knowledge_platform.mapper.*;
import com.itheima.knowledge_platform.pojo.dto.*;
import com.itheima.knowledge_platform.pojo.entity.*;
import com.itheima.knowledge_platform.pojo.vo.*;
import com.itheima.knowledge_platform.properties.AIUrlProperties;
import com.itheima.knowledge_platform.properties.FilePathProperties;
import com.itheima.knowledge_platform.result.PageResult;
import com.itheima.knowledge_platform.service.DocumentService;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DocumentServiceImpl implements DocumentService {

    @Autowired
    private DocumentMapper documentMapper;

    @Autowired
    private DocumentTagMapper documentTagMapper;

    @Autowired
    private DocumentVersionMapper documentVersionMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FilePathProperties filePathProperties;

    @Autowired
    private LikeMapper likeMapper;

    @Autowired
    private TeamMapper teamMapper;

    @Autowired
    private PlatformAdminMapper platformAdminMapper;

    @Autowired
    private TeamMemberMapper teamMemberMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AIUrlProperties aiUrlProperties;

/*    @Override
    public void updateContentPath(Long documentId, String finalPath) {
        //1.根据id查询文档
        Document document = documentMapper.getById(documentId);

        //检验是否为空
        if (document != null) {
            throw new DocumentNotFoundException(MessageConstant.DOCUMENT_NOT_FOUND);
        }

        //更新文档最终地址
        document.setContent(finalPath);

        //更新地址
        documentMapper.updateById(document);
    }*/

    /**
     * 创建文档
     *
     * @param documentDTO
     * @return
     */
    @Transactional
    @Override
    public DocumentVO save(DocumentDTO documentDTO) {
        //检验团队是否创建成功
        Team team = teamMapper.selectById(documentDTO.getTeamId());

        if (team == null || !team.getStatus().equals(TeamStatusEnum.CREATED.getCode())) {
            throw new TeamErrorException(MessageConstant.TEAM_NOT_EXIST);
        }


        //类型转换
        Document document = Document.builder()
                .title(documentDTO.getTitle())
                .categoryId(documentDTO.getCategoryId())
                .teamId(documentDTO.getTeamId())
                .status(documentDTO.getStatus())
                .version(0)
                .visibility(documentDTO.getVisibility())
                .authorId(BaseContext.getCurrentUserId())
                .build();

        //更新文档数据库
        documentMapper.insert(document);

        //更新文档-标签数据库
        List<Long> tagIds = documentDTO.getTagIds();
        for (Long tagId : tagIds) {
            DocumentTag build = DocumentTag.builder()
                    .tagId(tagId)
                    .documentId(document.getId())
                    .build();

            documentTagMapper.insert(build);
        }

        //根据id查询文档
        DocumentVO documentVO = new DocumentVO();
        Document document1 = documentMapper.getById(document.getId());
        //类型转换
        BeanUtils.copyProperties(document1, documentVO);

        return documentVO;
    }

    /**
     * 文档关联数据库
     * 上传/更新文档内容
     *
     * @param id
     * @param content
     * @return
     */
    @Transactional
    @Override
    public DocumentConnectVO connect(Long id, String content) throws IOException {
        //1.检测文档是否存在
        File file = new File(content);

        //检查是否存在
        if (!file.exists()) {
            throw new PathNotFoundException(MessageConstant.PATH_NOT_FOUND);
        }

        //检查是否是文件
        if (!file.isFile()) {
            throw new PathNotFoundException(MessageConstant.TEMP_DOCUMENT_NOT_FOUND);
        }

        //检查用户是否禁用状态
        Long userId = BaseContext.getCurrentUserId();
        User user = userMapper.getById(userId);
        if (Objects.equals(user.getStatus(), StatusConstant.DISABLE)) {
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //根据id检查文档是否存在
        Document document = documentMapper.getById(id);

        if (document == null) {
            throw new DocumentNotFoundException(MessageConstant.DOCUMENT_NOT_FOUND);
        }

        //检查文档状态，若文档处于已发布且待审核或者已通过审核，则不可修改
        if (Objects.equals(document.getStatus(), DocumentStatusEnum.PUBLISHED.getCode()) && (Objects.equals(document.getStatus(), AuditStatusEnum.PENDING.getCode()) || Objects.equals(document.getStatus(), AuditStatusEnum.APPROVED.getCode()))) {
            throw new DocumentLockedException(MessageConstant.DOCUMENT_LOCKED);
        }

        //文档处于已归档，不允许修改
        if (document.getStatus().equals(DocumentStatusEnum.ARCHIVED.getCode())) {
            throw new DocumentLockedException(MessageConstant.DOCUMENT_LOCKED);
        }

        //1.读取当前版本号和历史路径
        Integer currentVersion = document.getVersion();
        String oldContentPath = document.getContent();

        //检查文档当前是否有内容文件
        //有文件内容，说明是更新操作
        if (oldContentPath != null && !oldContentPath.isEmpty()) {
            // 旧文件保存历史版本
            // 1. 复制旧文件到版本快照路径
            String historyPath = filePathProperties.getHistoryPath() + document.getId() + "/v" + currentVersion + ".md";
            Files.copy(Paths.get(oldContentPath), Paths.get(historyPath));

            //更新文档版本表
            DocumentVersion documentVersion = DocumentVersion.builder()
                    .version(currentVersion)
                    .editorId(BaseContext.getCurrentUserId())
                    .documentId(document.getId())
                    .createdAt(LocalDateTime.now())
                    .changeRemark("更新文档内容")
                    .build();

            //增加版本数据
            documentVersionMapper.insert(documentVersion);
        }

        //移动临时文件到正式路径,覆盖原文件
        String formalPath = filePathProperties.getFormalPath() + document.getId() + "./content.md";
        Files.move(Paths.get(content), Paths.get(formalPath), StandardCopyOption.REPLACE_EXISTING);

        //更新 document 表
        Document doc = new Document();
        doc.setId(document.getId());
        doc.setContent(formalPath);
        doc.setVersion(document.getVersion());
        //乐观锁，不需要手动修改
        //doc.setVersion(currentVersion + 1);

        Integer rows = documentMapper.updateContentById(doc);

        //放在版本互相覆盖
        if (rows == 0) {
            throw new DocumentLockedException(MessageConstant.DOCUMENT_EDIT_FAILED);
        }

        return new DocumentConnectVO().builder()
                .id(id)
                .version(currentVersion + 1)
                .content(formalPath)
                .build();
    }

    /**
     * 更新文档元数据
     *
     * @param id
     * @param documentMetadataDTO
     */
    @Transactional
    @Override
    public void editMetadata(Long id, DocumentMetadataDTO documentMetadataDTO) {
        //查询文档是否存在
        Document document = documentMapper.getById(id);

        if (document == null) {
            throw new DocumentNotFoundException(MessageConstant.DOCUMENT_NOT_FOUND);
        }

        //更新文档表元数据数据库内容
        document.setTitle(documentMetadataDTO.getTitle());
        document.setCategoryId(documentMetadataDTO.getCategoryId());
        document.setTeamId(documentMetadataDTO.getTeamId());
        document.setStatus(documentMetadataDTO.getStatus());
        document.setVisibility(documentMetadataDTO.getVisibility());

        //更新元数据不需要乐观锁，因为不生成历史版本记录
        documentMapper.updateById(document);

        //更新文档标签表
        List<Long> tagIds = documentMetadataDTO.getTagIds();
        if (!CollectionUtils.isEmpty(tagIds)) {
            for (Long tagId : tagIds) {
                documentTagMapper.updateById(tagId, document.getId());
            }
        }
    }

    /**
     * 删除文档
     * 注意：只能删除草稿状态的文档,并且审核状态为编辑中或者已驳回状态
     * 删除文档还要删除 文档标签表内容 和 文档版本表内容
     *
     * @param id
     */
    @Transactional
    @Override
    public void deleteDocument(Long id) {
        //根据id查询文档
        Document document = documentMapper.getById(id);

        if (document == null) {
            throw new DocumentNotFoundException(MessageConstant.DOCUMENT_NOT_FOUND);
        }

        //检验状态
        if (document.getStatus().equals(DocumentStatusEnum.DRAFT.getCode()) && (document.getAuditStatus().equals(AuditStatusEnum.EDITING.getCode()) || document.getAuditStatus().equals(AuditStatusEnum.REJECTED.getCode()))) {
            //删除文档表内容
            documentMapper.deleteById(document.getId());

            //删除文档标签表内容
            documentTagMapper.deleteByDocumentId(document.getId());

            //删除文档版本表内容
            documentVersionMapper.deleteByDocumentId(document.getId());
        }
    }

    /**
     * 文档列表分页查询
     *
     * @param documentQueryPageDTO
     * @return
     */
    @Override
    public PageResult<DocumentPageVO> page(DocumentQueryPageDTO documentQueryPageDTO) {
        //开始分页
        PageHelper.startPage(documentQueryPageDTO.getPage(), documentQueryPageDTO.getSize());

        //如果是想要见到私有，查询文档是否包含自己是作者的
        Long userId = BaseContext.getCurrentUserId();
        documentQueryPageDTO.setAuthorId(userId);

        //如果想见团队内文档，可见范围=1，团队id不可空
        if (documentQueryPageDTO.getVisibility() == DocumentVisibilityEnum.WITHIN_THE_TEAM.getCode() && documentQueryPageDTO.getTeamId() == null) {
            throw new QueryErrorException(MessageConstant.TEAMID_NOT_ALLOWED_NULL);
        }

        //如果团队id不为空，可见范围为空，补全可见范围
        if (documentQueryPageDTO.getTeamId() != null && documentQueryPageDTO.getVisibility() == null) {
            documentQueryPageDTO.setVisibility(DocumentVisibilityEnum.WITHIN_THE_TEAM.getCode());
        }


        Page<DocumentPageVO> documentPageVOPage = documentMapper.pageQuery(documentQueryPageDTO);

        //添加创建人账号
        for (DocumentPageVO documentPageVO : documentPageVOPage) {
            User user = userMapper.getById(documentPageVO.getAuthorId());

            //检验账号是否存在
            if (user == null) {
                throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
            }

            documentPageVO.setAuthorName(user.getUsername());
        }

        return new PageResult(documentPageVOPage.getTotal(), documentPageVOPage.getPageNum(), documentPageVOPage.getPageSize(), documentPageVOPage.getResult());
    }

    /**
     * 发布文档
     *
     * @param id
     */
    @Override
    public void publish(Long id) {
        //根据id查询文档
        Document document = documentMapper.getById(id);

        if (document == null) {
            throw new DocumentNotFoundException(MessageConstant.DOCUMENT_NOT_FOUND);
        }

        //文档状态：草稿→发布
        document.setStatus(DocumentStatusEnum.PUBLISHED.getCode());
        document.setAuditStatus(AuditStatusEnum.PENDING.getCode());

        //发布文档不需要乐观锁，不生产历史版本，避免消耗时间，频繁产生冲突
        documentMapper.updateById(document);
    }

    /**
     * 归档文档
     *
     * @param id
     */
    @Transactional
    @Override
    public void archive(Long id) {
        //根据id查询文档
        Document document = documentMapper.getById(id);

        if (document == null) {
            throw new DocumentNotFoundException(MessageConstant.DOCUMENT_NOT_FOUND);
        }

        //文档状态 改成 已发布
        document.setStatus(DocumentStatusEnum.ARCHIVED.getCode());
        //审核状态改成已通过
        document.setAuditStatus(AuditStatusEnum.APPROVED.getCode());

        //归档文档不需要乐观锁，不生产历史版本，避免消耗时间，频繁产生冲突
        documentMapper.updateById(document);

        try {
            // 构建完整的 URL，包含查询参数
            String url = UriComponentsBuilder
                    .fromHttpUrl(aiUrlProperties.getAgentBaseUrl() + "/document/archive")
                    .queryParam("document_path", document.getContent())
                    .toUriString();

            restTemplate.postForLocation(url, null);
        } catch (Exception e) {
            log.error("归档文档失败, document_path: {}", document.getContent(), e);
        }
    }

    /**
     * 点赞/取消点赞文档
     * 需要管理收藏表
     *
     * @param id
     */
    @Transactional
    @Override
    public void like(Long id) {
        //根据id查询文档
        Document document = documentMapper.getById(id);

        if (document == null) {
            throw new DocumentNotFoundException(MessageConstant.DOCUMENT_NOT_FOUND);
        }

        //只有 审核通过 且 已发布或已归档 才能点赞/取消点赞
        if (!document.getAuditStatus().equals(AuditStatusEnum.APPROVED.getCode()) || Objects.equals(document.getStatus(), DocumentStatusEnum.DRAFT.getCode())) {
            throw new DocumentLockedException(MessageConstant.DOCUMENT_LOCKED);
        }

        //检查是否点过赞
        Like like1 = likeMapper.getByDocumentIdAndUserId(document.getId(), BaseContext.getCurrentUserId());

        if (like1 == null) {
            //没点赞
            Like like = Like.builder()
                    .userId(BaseContext.getCurrentUserId())
                    .documentId(document.getId())
                    .createdAt(LocalDateTime.now())
                    .build();
            likeMapper.insert(like);
            documentMapper.incLikeCount(document.getId()); // 点赞数+1
        } else {
            //点过赞
            likeMapper.deleteById(like1.getId());
            documentMapper.decLikeCount(document.getId());
        }
    }

    /**
     * 获取文档历史版本列表
     *
     * @param id
     * @return
     */
    @Override
    public List<DocumentVersionListPageVO> version(Long id) {
        //根据id查询文档
        Document document = documentMapper.getById(id);

        if (document == null) {
            throw new DocumentNotFoundException(MessageConstant.DOCUMENT_NOT_FOUND);
        }

        //通过文档id查询文本版本是否存在
        List<DocumentVersion> list = documentVersionMapper.selectByDocumentId(document.getId());

        //类型转换-----流方法
        List<DocumentVersionListPageVO> documentVersionPageList = list.stream().map(documentVersion -> {
            DocumentVersionListPageVO documentVersionListPageVO = new DocumentVersionListPageVO();
            BeanUtils.copyProperties(documentVersion, documentVersionListPageVO);
            //根据编辑者id查询账号
            User user = userMapper.getById(documentVersion.getEditorId());

            if (user == null) {
                throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
            }

            documentVersionListPageVO.setEditorName(user.getUsername());

            return documentVersionListPageVO;
        }).collect(Collectors.toList());

        return documentVersionPageList;
    }

    /**
     * 版本回滚
     */
    @Transactional
    @Override
    public void rollback(Long id, Integer version) throws IOException {
        //根据id查询文档
        Document document = documentMapper.getById(id);

        if (document == null) {
            throw new DocumentNotFoundException("[rollback]" + MessageConstant.DOCUMENT_NOT_FOUND);
        }

        //校验传入的 version 版本号是否合法
        //不能小于 1
        //不能大于当前文档最大历史版本号
        if (version < 1 || version > document.getVersion()) {
            throw new DocumentVersionIllegalException("[rollback]" + MessageConstant.DOCUMENT_VERSION_ILLEGAL);
        }

        //只有文档处于操作中才可回滚
        if (!document.getStatus().equals(DocumentStatusEnum.DRAFT.getCode())) {
            throw new DocumentLockedException("[rollback]" + MessageConstant.DOCUMENT_LOCKED);
        }

        //只有审核状态是编辑中或者已驳回才可回滚
        if (document.getAuditStatus().equals(AuditStatusEnum.PENDING.getCode()) || document.getAuditStatus().equals(AuditStatusEnum.APPROVED.getCode())) {
            throw new DocumentLockedException("[rollback]" + MessageConstant.DOCUMENT_LOCKED);
        }

        //文档版本表更新
        Integer currentVersion = document.getVersion();
        String oldContentPath = document.getContent();

        if (oldContentPath != null && !oldContentPath.isEmpty()) {
            // 旧文件保存历史版本
            // 1. 复制旧文件到版本快照路径
            String historyPath = filePathProperties.getHistoryPath() + document.getId() + "/v" + currentVersion + ".md";
            Files.copy(Paths.get(oldContentPath), Paths.get(historyPath));

            //更新文档版本表
            DocumentVersion documentVersion = DocumentVersion.builder()
                    .version(currentVersion)
                    .editorId(BaseContext.getCurrentUserId())
                    .documentId(document.getId())
                    .createdAt(LocalDateTime.now())
                    .changeRemark("被回滚挤下去的文档")
                    .build();

            //增加版本数据
            documentVersionMapper.insert(documentVersion);
        }

        //根据文档id和历史版本找寻版本内容
        DocumentVersion documentVersion = documentVersionMapper.selectByDocumentIdAndVersion(document.getId(), version);

        //移动临时文件到正式路径,覆盖原文件
        String formalPath = filePathProperties.getFormalPath() + document.getId() + "./content.md";
        Files.copy(Paths.get(documentVersion.getContent()), Paths.get(formalPath), StandardCopyOption.REPLACE_EXISTING);

        //更新 document 表
        Document doc = new Document();
        doc.setId(document.getId());
        doc.setContent(formalPath);
        //乐观锁，不需要手动修改
        //doc.setVersion(currentVersion + 1);

        Integer rows = documentMapper.updateContentById(doc);

        //放在版本互相覆盖
        if (rows == 0) {
            throw new DocumentLockedException("[rollback]" + MessageConstant.DOCUMENT_EDIT_FAILED);
        }
    }

    /**
     * 获取待审核文档列表
     *
     * @param page
     * @param size
     * @param title
     * @return
     */
    @Override
    public PageResult<DocumentAdminVO> adminReviewPage(Integer page, Integer size, String title) {
        //从请求头获取 Token，解析出当前管理员身份
        Long currentAdminId = BaseContext.getCurrentAdminId();

        //校验是否有权限（超管或子管理员）
        PlatformAdmin platformAdmin = platformAdminMapper.selectById(currentAdminId);
        if (platformAdmin == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //开始分页
        PageHelper.startPage(page, size);

        //查询所有 audit_status = 0（待审核）的文档
        //如果有关键词，增加 title LIKE '%keyword%' 条件
        List<Document> documents = documentMapper.selectByAuditStatusAndTitle(DocumentAuditStatusEnum.PENDING_REVIEW.getCode(), title);

        Page<Document> documentPage = (Page<Document>) documents;

        //关联 user 表获取作者用户名、邮箱
        //关联 team 表获取团队名称（如果 team_id 不为空）
        List<DocumentAdminVO> list = documents.stream()
                .map(document -> {
                    //作者信息
                    User user = userMapper.getById(document.getAuthorId());

                    //类型转换
                    DocumentAdminVO documentAdminVO = new DocumentAdminVO();
                    BeanUtils.copyProperties(document, documentAdminVO);

                    documentAdminVO.setAuthorName(user.getUsername());
                    documentAdminVO.setAuthorEmail(user.getEmail());

                    if (document.getTeamId() != null) {
                        //团队信息
                        Team team = teamMapper.selectById(document.getTeamId());
                        documentAdminVO.setTeamName(team.getName());
                    }

                    return documentAdminVO;
                }).collect(Collectors.toList());

        return new PageResult<>(documentPage.getTotal(), documentPage.getPageNum(), documentPage.getPageSize(), list);
    }

    /**
     * 获取所有文档列表
     *
     * @param documentAdminDetailPageDTO
     * @return
     */
    @Override
    public PageResult<DocumentAdminDetailPageVO> adminPage(DocumentAdminDetailPageDTO documentAdminDetailPageDTO) {
        //从请求头获取 Token，解析出当前管理员身份
        Long currentAdminId = BaseContext.getCurrentAdminId();

        //校验是否有权限（超管或子管理员）
        PlatformAdmin platformAdmin = platformAdminMapper.selectById(currentAdminId);
        if (platformAdmin == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //构建分页查询对象（PageHelper 或手动分页）
        PageHelper.startPage(documentAdminDetailPageDTO.getPage(), documentAdminDetailPageDTO.getSize());

        /**
         * 主表查询条件：status IN (1, 2)，audit_status IN (1, 2, 3)
         * 如果有 auditStatus 参数，精确匹配
         * 如果有 title 关键字，LIKE 模糊匹配
         * 如果有 authorName，需要 JOIN user 表或子查询
         */
        List<Document> documents = documentMapper.selectByDocumentAdminDetailPageDTO(documentAdminDetailPageDTO);

        Page<Document> documentPage = (Page<Document>) documents;

        //组装返回结果，补充状态的中文名称
        List<DocumentAdminDetailPageVO> vos = documents.stream()
                .map(po -> {
                    DocumentAdminDetailPageVO vo = new DocumentAdminDetailPageVO();

                    BeanUtils.copyProperties(po, vo);
                    vo.setStatusName(DocumentStatusEnum.getByCode(vo.getStatus()).getMessage());
                    vo.setAuditStatusName(DocumentAuditStatusEnum.getByCode(vo.getAuditStatus()).getMessage());

                    User user = userMapper.getById(po.getAuthorId());
                    vo.setAuthorName(user.getUsername());

                    Team team = teamMapper.selectById(po.getTeamId());
                    vo.setTeamName(team.getName());

                    return vo;
                }).collect(Collectors.toList());

        return new PageResult<>(documentPage.getTotal(), documentPage.getPageNum(), documentPage.getPageSize(), vos);
    }

    /**
     * 审核文档
     *
     * @param id
     * @param documentReviewAdminDTO
     * @return
     */
    @Override
    public DocumentReviewAdminVO adminReview(Long id, DocumentReviewAdminDTO documentReviewAdminDTO) {
        //从请求头获取 Token，解析出当前管理员身份
        Long currentAdminId = BaseContext.getCurrentAdminId();

        //校验是否有权限（超管或子管理员）
        PlatformAdmin platformAdmin = platformAdminMapper.selectById(currentAdminId);
        if (platformAdmin == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //校验请求参数（auditStatus 有效性，驳回时 rejectReason 必填）
        if (documentReviewAdminDTO.getAuditStatus().equals(DocumentAuditStatusEnum.REJECTED.getCode()) && documentReviewAdminDTO.getRejectReason().isEmpty()) {
            throw new DocumentErrorException(MessageConstant.REJECT_REASON_NOT_NULL);
        }

        //根据文档ID查询文档
        Document document = documentMapper.getById(id);
        //校验文档是否存在，不存在返回404
        if (document == null) {
            throw new DocumentNotFoundException(MessageConstant.DOCUMENT_NOT_FOUND);
        }

        //校验文档 audit_status 是否为 1（待审核），不是则返回错误
        if (!document.getAuditStatus().equals(DocumentAuditStatusEnum.PENDING_REVIEW.getCode())) {
            throw new DocumentErrorException(MessageConstant.AUDIT_STATUS_ERROR);
        }

        //校验文档 status 不是草稿（0）
        if (document.getStatus().equals(DocumentStatusEnum.DRAFT.getCode())) {
            throw new DocumentErrorException(MessageConstant.DOCUMENT_STATUS_ERROR);
        }

        //开启数据库事务-----审核状态乐观锁
        document.setAuditStatus(documentReviewAdminDTO.getAuditStatus());
        document.setRejectReason(documentReviewAdminDTO.getRejectReason());
        Integer rows = documentMapper.updateReviewById(document);

        if (rows == 0) {
            throw new DocumentErrorException(MessageConstant.DOCUMENT_LOCKED);
        }

        /**
         * TODO 发送通知：
         * 审核通过：通知作者"您的文档已通过审核"
         * 审核驳回：通知作者"您的文档被驳回，原因：xxx"
         */

        return DocumentReviewAdminVO.builder()
                .id(document.getId())
                .auditStatus(documentReviewAdminDTO.getAuditStatus())
                .rejectReason(documentReviewAdminDTO.getRejectReason())
                .build();
    }

    /**
     * 用户是否有权限读取文档
     *
     * @param id
     * @param userId
     * @return
     */
    @Override
    public Boolean hasReadPermission(Long id, Long userId) {
        //用户端：校验用户是否有权限查看该文档（所有者、团队成员、公开文档）
        Document document = documentMapper.getById(id);
        if (document == null) {
            throw new DocumentErrorException(MessageConstant.DOCUMENT_NOT_FOUND);
        }

        //私有
        if (document.getVisibility().equals(DocumentVisibilityEnum.PRIVATE.getCode())) {
            if (document.getAuthorId().equals(userId)) {
                return true;
            }
            throw new DocumentErrorException(MessageConstant.NO_PERMISSION_TO_READ_DOCUMENT);
        }

        //团队
        if (document.getVisibility().equals(DocumentVisibilityEnum.WITHIN_THE_TEAM.getCode())) {
            List<TeamMember> teamMembers = teamMemberMapper.selectTeamMemberByTeamId(document.getTeamId());
            List<Long> userIds = teamMembers.stream()
                    .map(TeamMember::getUserId)
                    .toList();

            if (userIds.contains(userId)) {
                return true;
            }
            throw new DocumentErrorException(MessageConstant.USER_NOT_IN_TEAM);
        }

        //公开
        return true;
    }

    /**
     *
     * @param documentId
     * @param startLine
     * @param linesPerBatch
     * @param speed
     * @param emitter
     */
    @Override
    public void streamContentByLine(Long documentId, Long startLine, Integer linesPerBatch, String speed, SseEmitter emitter) {
        // 使用线程池异步执行，避免阻塞Tomcat线程
        CompletableFuture.runAsync(() -> {
            BufferedReader reader = null;
            try {
                // 1. 查询文档，获取文件路径
                Document document = documentMapper.getById(documentId);
                if (document == null || StringUtils.isBlank(document.getContent())) {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data(Map.of("code", 20001, "message", "文档或内容不存在")));
                    emitter.complete();
                    return;
                }

                File file = new File(document.getContent());
                if (!file.exists()) {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data(Map.of("code", 20007, "message", "文档文件不存在")));
                    emitter.complete();
                    return;
                }

                // 2. 打开文件，按行读取
                reader = new BufferedReader(
                        new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)
                );

                // 3. 跳过前面的行（定位到起始位置）
                long currentLine = 1;
                if (startLine > 1) {
                    while (currentLine < startLine && reader.readLine() != null) {
                        currentLine++;
                    }
                }

                // 4. 先统计总行数（可选，如果不统计可以传-1）
                long totalLines = countLines(file);

                // 5. 发送开始事件
                emitter.send(SseEmitter.event()
                        .name("start")
                        .data(Map.of(
                                "totalLines", totalLines,
                                "startLine", startLine
                        )));

                // 6. 循环读取并推送
                StringBuilder batch = new StringBuilder();
                int lineCountInBatch = 0;
                String line;
                boolean isSmooth = "smooth".equalsIgnoreCase(speed);

                while ((line = reader.readLine()) != null) {
                    batch.append(line).append("\n");
                    lineCountInBatch++;
                    currentLine++;

                    // 凑够一批就推送
                    if (lineCountInBatch >= linesPerBatch) {
                        // 发送内容块
                        emitter.send(SseEmitter.event()
                                .name("lines")
                                .data(batch.toString()));

                        // 发送进度
                        emitter.send(SseEmitter.event()
                                .name("progress")
                                .data(Map.of(
                                        "currentLine", currentLine - 1,
                                        "totalLines", totalLines,
                                        "percent", (double) (currentLine - 1) / totalLines
                                )));

                        // 清空缓冲区
                        batch.setLength(0);
                        lineCountInBatch = 0;

                        // 模拟打字机效果
                        if (isSmooth) {
                            Thread.sleep(50);
                        }

                        // 检查前端是否断开连接
                        if (Thread.currentThread().isInterrupted()) {
                            break;
                        }
                    }
                }

                // 7. 发送剩余内容
                if (batch.length() > 0) {
                    emitter.send(SseEmitter.event()
                            .name("lines")
                            .data(batch.toString()));
                }

                // 8. 发送结束事件
                emitter.send(SseEmitter.event()
                        .name("end")
                        .data(Map.of("totalLinesSent", currentLine - 1)));

                emitter.complete();

            } catch (Exception e) {
                log.error("流式输出文档内容失败，文档ID: {}", documentId, e);
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data(Map.of("code", 500, "message", e.getMessage())));
                } catch (IOException ex) {
                    log.error("发送错误事件失败", ex);
                }
                emitter.completeWithError(e);
            } finally {
                try {
                    if (reader != null) reader.close();
                } catch (IOException e) {
                    log.error("关闭文件流失败", e);
                }
            }
        });
    }

    /**
     * 统计文件总行数（大数据文件优化）
     */
    private long countLines(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            long lines = 0;
            while (reader.readLine() != null) {
                lines++;
            }
            return lines;
        }
    }

    /**
     * 流式输出文档内容（按字节流，处理UTF-8边界）
     * 适用于超大文件，支持断点续传
     */
    @Override
    public void streamContentByByte(Long documentId, Long start,
                                    Integer chunkSize, String speed,
                                    SseEmitter emitter) {
        CompletableFuture.runAsync(() -> {
            RandomAccessFile raf = null;
            try {
                // 1. 查询文档，获取文件路径
                Document document = documentMapper.getById(documentId);
                if (document == null || StringUtils.isBlank(document.getContent())) {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data(Map.of("code", 20001, "message", "文档或内容不存在")));
                    emitter.complete();
                    return;
                }

                File file = new File(document.getContent());
                if (!file.exists()) {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data(Map.of("code", 20007, "message", "文档文件不存在")));
                    emitter.complete();
                    return;
                }

                long fileSize = file.length();

                // 2. 打开文件，定位到起始位置
                raf = new RandomAccessFile(file, "r");
                raf.seek(start);

                // 3. 发送开始事件
                emitter.send(SseEmitter.event()
                        .name("start")
                        .data(Map.of(
                                "totalSize", fileSize,
                                "start", start
                        )));

                // 4. 循环读取并推送
                byte[] buffer = new byte[chunkSize];
                long currentOffset = start;
                boolean isSmooth = "smooth".equalsIgnoreCase(speed);
                long lastProgressSent = 0;

                while (currentOffset < fileSize) {
                    // 计算本次读取大小（最后一块可能不够）
                    int bytesToRead = (int) Math.min(chunkSize, fileSize - currentOffset);
                    int bytesRead = raf.read(buffer, 0, bytesToRead);

                    if (bytesRead <= 0) break;

                    // 处理UTF-8边界：检查最后一个字节是否是多字节字符的一部分
                    int validBytes = bytesRead;
                    if (currentOffset + bytesRead < fileSize) {
                        validBytes = findUtf8Boundary(buffer, bytesRead);
                    }

                    // 读取有效内容
                    String content = new String(buffer, 0, validBytes, StandardCharsets.UTF_8);

                    // 发送内容块
                    emitter.send(SseEmitter.event()
                            .name("chunk")
                            .data(content));

                    // 更新偏移量
                    currentOffset += validBytes;

                    // 如果有多余字节（被截断的字符），需要回退
                    if (validBytes < bytesRead) {
                        int remainder = bytesRead - validBytes;
                        raf.seek(raf.getFilePointer() - remainder);
                        currentOffset -= remainder;
                    }

                    // 每推送10%发送一次进度
                    if (currentOffset - lastProgressSent > fileSize * 0.1) {
                        emitter.send(SseEmitter.event()
                                .name("progress")
                                .data(Map.of(
                                        "offset", currentOffset,
                                        "total", fileSize,
                                        "percent", (double) currentOffset / fileSize
                                )));
                        lastProgressSent = currentOffset;
                    }

                    // 模拟打字机效果
                    if (isSmooth) {
                        Thread.sleep(30);
                    }

                    // 检查前端是否断开
                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }
                }

                // 5. 发送结束事件
                emitter.send(SseEmitter.event()
                        .name("end")
                        .data(Map.of("totalSent", currentOffset)));

                emitter.complete();

            } catch (Exception e) {
                log.error("流式输出文档内容失败", e);
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data(Map.of("code", 500, "message", e.getMessage())));
                } catch (IOException ex) {
                    log.error("发送错误事件失败", ex);
                }
                emitter.completeWithError(e);
            } finally {
                try {
                    if (raf != null) raf.close();
                } catch (IOException e) {
                    log.error("关闭文件失败", e);
                }
            }
        });
    }

    /**
     * 查找UTF-8字符边界
     * 返回不超过bytesRead的最大有效字节数（保证不会切断多字节字符）
     */
    private int findUtf8Boundary(byte[] buffer, int bytesRead) {
        // 从最后一个字节往前找，直到找到一个合法的字符边界
        for (int i = bytesRead - 1; i >= 0; i--) {
            byte b = buffer[i];
            // ASCII字符或UTF-8单字节字符（0x00-0x7F）
            if (b >= 0) {
                return i + 1;
            }
            // UTF-8多字节字符的起始字节（110xxxxx, 1110xxxx, 11110xxx）
            if ((b & 0xC0) == 0xC0) {
                // 计算这个字符应该有多少字节
                int charBytes = 1;
                if ((b & 0xE0) == 0xC0) charBytes = 2;
                else if ((b & 0xF0) == 0xE0) charBytes = 3;
                else if ((b & 0xF8) == 0xF0) charBytes = 4;

                // 检查这个完整字符是否在buffer内
                if (i + charBytes <= bytesRead) {
                    return i + charBytes;
                } else {
                    // 字符被切断，返回i位置（不含这个字符）
                    return i;
                }
            }
            // 如果是续字节（10xxxxxx），继续往前找
        }
        return 0;
    }
}
