package com.itheima.knowledge_platform.mapper;

import com.itheima.knowledge_platform.pojo.entity.Category;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CategoryMapper {

    /**
     * 通过团队id查询分类
     * @param teamId
     * @return
     */
    List<Category> selectByTeamId(Long teamId);

    @Select("select * from category where parent_id=#{parentId} and name=#{name} and team_id=#{teamId}")
    Category selectExistById(Long parentId, String name,Long teamId);

    @Select("select * from category where parent_id=#{parentId}")
    List<Category> selectByParentId(Long parentId);

    /**
     * 主键回显
     * 添加数据
     * @param category
     */
    @Insert("insert into category (parent_id, name, team_id, sort_order, created_at)\n" +
            "VALUES (#{parentId},#{name},#{teamId},#{sortOrder},#{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void insert(Category category);

    /**
     * 根据分类ID查询
     * @param id
     * @return
     */
    @Select("select * from category where id=#{id}")
    Category selectById(Long id);

    /**
     * 更新分类
     * @param category
     */
    @Update("update category set name=#{name},sort_order=#{sortOrder},parent_id=#{parentId} where id=#{id}")
    void updateById(Category category);

    /**
     * 根据ID删除
     * @param id
     */
    @Delete("delete from category where id=#{id}")
    void deleteById(Long id);

    @Delete("delete from category where team_id=#{teamId}")
    void deleteByTeamId(Long id);
}
