package com.itheima.knowledge_platform.mapper;

import com.github.pagehelper.Page;
import com.itheima.knowledge_platform.pojo.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper {

    /**
     * 根据用户账号搜索用户信息
     * @param username
     * @return
     */
    @Select("select * from user where username=#{username}")
    User getByUsername(String username);

    /**
     * 新增用户
     * @param user
     */
    @Insert("insert into user (username, password, email, avatar, status, created_at)" +
            " VALUES (#{username},#{password},#{email},#{avatar},#{status},#{createdAt})")
    void insert(User user);

    /**
     * 更改用户信息
     * @param user
     */
    void update(User user);

    /**
     * 根据id查询用户
     * @param id
     * @return
     */
    @Select("select * from user where id=#{id}")
    User getById(Long id);

    /**
     * 动态查询
     * @param user
     * @return
     */
    List<User> selectByUser(User user);
}
