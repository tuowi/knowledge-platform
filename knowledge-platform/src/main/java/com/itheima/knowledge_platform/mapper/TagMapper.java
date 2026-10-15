package com.itheima.knowledge_platform.mapper;

import com.itheima.knowledge_platform.pojo.entity.Tags;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TagMapper {

    /**
     * 根据 teamId 查询该团队下所有标签
     * @param teamId
     * @return
     */
    @Select("select * from tags where team_id=#{teamId} order by created_at desc")
    List<Tags> selectByTeamId(Long teamId);

    /**
     * 校验该团队下标签名不能重复
     * @param name
     * @param teamId
     * @return
     */
    @Select("select * from tags where team_id=#{teamId} and name=#{name}")
    Tags selectRepeatByTeamIdAndName(String name, Long teamId);

    /**
     * 插入数据
     * @param tags
     */
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    @Insert("insert into tags (name, team_id, created_at) VALUES (#{name},#{teamId},#{createdAt})")
    void insert(Tags tags);

    /**
     * 根据ID查询
     * @param id
     * @return
     */
    @Select("select * from tags where id=#{id}")
    Tags selectById(Long id);

    /**
     * 根据id删除
     * @param id
     */
    @Delete("delete from tags where id=#{id}")
    void deleteById(Long id);

    /**
     * 删除
     */
    @Delete("delete from tags where team_id=#{teamId}")
    void deleteByTeamId(Long teamId);
}
