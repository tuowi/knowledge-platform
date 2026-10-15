package com.itheima.knowledge_platform.service.Impl;

import com.itheima.knowledge_platform.constant.MessageConstant;
import com.itheima.knowledge_platform.exception.QueryErrorException;
import com.itheima.knowledge_platform.exception.TagErrorException;
import com.itheima.knowledge_platform.mapper.DocumentTagMapper;
import com.itheima.knowledge_platform.mapper.TagMapper;
import com.itheima.knowledge_platform.pojo.entity.Tags;
import com.itheima.knowledge_platform.pojo.vo.TagsVO;
import com.itheima.knowledge_platform.service.TagService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TagServiceImpl implements TagService {

    @Autowired
    private TagMapper tagMapper;
    @Autowired
    private DocumentTagMapper documentTagMapper;

    /**
     * 获取标签列表
     *
     * @param teamId
     * @return
     */
    @Override
    public List<Tags> list(Long teamId) {
        //1.校验 teamId 不能为空
        if (teamId == null) {
            throw new TagErrorException("[list]" + MessageConstant.TEAMID_NOT_ALLOWED_NULL);
        }

        //根据 teamId 查询该团队下所有标签
        List<Tags> tagses = tagMapper.selectByTeamId(teamId);

        return tagses;
    }

    /**
     * 创建标签
     *
     * @param name
     * @param teamId
     * @return
     */
    @Override
    public TagsVO create(String name, Long teamId) {
        //校验 name 不为空
        //校验 teamId 不为空
        if (name == null || teamId == null) {
            throw new QueryErrorException("[create]" + MessageConstant.QUERY_LACK);
        }

        //校验该团队下标签名不能重复（uk_team_tag 唯一索引）
        Tags tags = tagMapper.selectRepeatByTeamIdAndName(name, teamId);

        if (tags!=null){
            throw new TagErrorException(MessageConstant.TAG_NAME_EXIST);
        }

        //插入 tag 表
        Tags tags1 = Tags.builder()
                .createdAt(LocalDateTime.now())
                .name(name)
                .teamId(teamId)
                .build();
        tagMapper.insert(tags1);

        //类型转换
        TagsVO tagsVO=new TagsVO();
        BeanUtils.copyProperties(tags1,tagsVO);
        return tagsVO;
    }

    /**
     * 删除标签
     * @param id
     */
    @Transactional
    @Override
    public void delete(Long id) {
        //根据 id 查询标签是否存在，不存在抛 404
        Tags tags=tagMapper.selectById(id);

        if (tags==null){
            throw new TagErrorException(MessageConstant.TAG_NOT_FOUND);
        }

        //删除标签（物理删除）
        tagMapper.deleteById(id);

        //删除文档与标签关联表的关联关系
        documentTagMapper.deleteByTagsId(id);
    }
}
