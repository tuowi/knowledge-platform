package com.itheima.knowledge_platform.service.Impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itheima.knowledge_platform.constant.MessageConstant;
import com.itheima.knowledge_platform.context.BaseContext;
import com.itheima.knowledge_platform.enums.DocumentStatusEnum;
import com.itheima.knowledge_platform.enums.DocumentVisibilityEnum;
import com.itheima.knowledge_platform.exception.AccountNotFoundException;
import com.itheima.knowledge_platform.exception.DocumentNotAllowedFavoritedException;
import com.itheima.knowledge_platform.exception.DocumentNotFoundException;
import com.itheima.knowledge_platform.mapper.DocumentMapper;
import com.itheima.knowledge_platform.mapper.FavoriteMapper;
import com.itheima.knowledge_platform.mapper.TeamMemberMapper;
import com.itheima.knowledge_platform.mapper.UserMapper;
import com.itheima.knowledge_platform.pojo.entity.Document;
import com.itheima.knowledge_platform.pojo.entity.Favorite;
import com.itheima.knowledge_platform.pojo.entity.TeamMember;
import com.itheima.knowledge_platform.pojo.entity.User;
import com.itheima.knowledge_platform.pojo.vo.FavoriteDocumentVO;
import com.itheima.knowledge_platform.result.PageResult;
import com.itheima.knowledge_platform.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private DocumentMapper documentMapper;

    @Autowired
    private TeamMemberMapper teamMemberMapper;
    @Autowired
    private UserMapper userMapper;

    /**
     * 收藏文档
     *
     * @param documentId
     */
    @Transactional
    @Override
    public void favorite(Long documentId) {
        //文档存在性：文档ID是否存在
        Document document = documentMapper.getById(documentId);

        if (document == null) {
            throw new DocumentNotFoundException("[favorite]" + MessageConstant.DOCUMENT_NOT_FOUND);
        }

        //"已发布"或"已归档"状态，且审核已通过
        if (document.getStatus().equals(DocumentStatusEnum.DRAFT.getCode())) {
            throw new DocumentNotAllowedFavoritedException("[favorite]" + MessageConstant.DOCUMENT_NOT_ALLOWED_FAVORITE);
        }

        //文档是否对当前用户可见：
        //公开文档（visibility=2）：所有人都可收藏
        //团队内文档（visibility=1）：只有团队成员可收藏
        //私有文档（visibility=0）：只有作者自己可收藏
        if (document.getVisibility().equals(DocumentVisibilityEnum.WITHIN_THE_TEAM.getCode())) {
            //得到文档所属团队
            Long teamId = document.getTeamId();

            //根据团队成员关联表查询收藏用户是否属于该团队
            TeamMember teamMember = teamMemberMapper.selectByTeamIdAndUserId(teamId, BaseContext.getCurrentUserId());

            if (teamMember == null) {
                throw new DocumentNotFoundException("[favorite]" + MessageConstant.DOCUMENT_NOT_ALLOWED_FAVORITE);
            }
        } else if (document.getVisibility().equals(DocumentVisibilityEnum.PRIVATE.getCode())) {
            //查看文档创建者是否为本人
            if (!document.getAuthorId().equals(BaseContext.getCurrentUserId())) {
                throw new DocumentNotFoundException("[favorite]" + MessageConstant.DOCUMENT_NOT_ALLOWED_FAVORITE);
            }
        }

        Favorite favorite = Favorite.builder()
                .documentId(document.getId())
                .userId(BaseContext.getCurrentUserId())
                .createdAt(LocalDateTime.now())
                .build();
        favoriteMapper.insert(favorite);

        //文档表收藏记录更新
        documentMapper.incFavoriteCount(document.getId());
    }

    /**
     * 取消收藏
     *
     * @param documentId
     */
    @Transactional
    @Override
    public void cancel(Long documentId) {
        //文档存在性：文档ID是否存在
        Document document = documentMapper.getById(documentId);

        if (document == null) {
            throw new DocumentNotFoundException("[cancel]" + MessageConstant.DOCUMENT_NOT_FOUND);
        }

//        收藏记录属于当前用户（只能取消自己的收藏）
        Favorite favorite = favoriteMapper.getByDocumentIdAndUserId(document.getId(), BaseContext.getCurrentUserId());

        if (favorite == null) {
            throw new DocumentNotAllowedFavoritedException("[cancel]" + MessageConstant.FAVORITE_NOT_FOUND);
        }

        favoriteMapper.deleteById(favorite.getId());

        //文档表收藏记录更新
        documentMapper.decFavoriteCount(document.getId());
    }

    /**
     * 获取我的收藏列表
     *
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageResult<FavoriteDocumentVO> page(Integer page, Integer size) {
        PageHelper.startPage(page, size);

        Page<FavoriteDocumentVO> favoriteDocumentVOS = favoriteMapper.pageQuery(BaseContext.getCurrentUserId());

        //根据作者id获取作者账号
        List<FavoriteDocumentVO> list = favoriteDocumentVOS.stream().map(favoriteDocumentVO -> {
            //查看账号是否存在
            User user = userMapper.getById(favoriteDocumentVO.getAuthorId());

            if (user == null) {
                throw new AccountNotFoundException("[page]" + MessageConstant.ACCOUNT_NOT_FOUND);
            }

            favoriteDocumentVO.setAuthorName(user.getUsername());

            return favoriteDocumentVO;
        }).collect(Collectors.toList());

        return new PageResult<>(favoriteDocumentVOS.getTotal(), favoriteDocumentVOS.getPageNum(), favoriteDocumentVOS.getPageSize(), favoriteDocumentVOS.getResult());
    }
}
