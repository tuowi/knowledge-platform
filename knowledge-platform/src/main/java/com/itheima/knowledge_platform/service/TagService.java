package com.itheima.knowledge_platform.service;

import com.itheima.knowledge_platform.pojo.entity.Tags;
import com.itheima.knowledge_platform.pojo.vo.TagsVO;

import java.util.List;

public interface TagService {

    /**
     * 获取标签列表
     * @param teamId
     * @return
     */
    List<Tags> list(Long teamId);

    /**
     * 创建标签
     * @param name
     * @param teamId
     * @return
     */
    TagsVO create(String name, Long teamId);

    /**
     * 删除标签
     * @param id
     */
    void delete(Long id);
}
