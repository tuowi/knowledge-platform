package com.itheima.knowledge_platform.mapper;

import com.itheima.knowledge_platform.pojo.entity.PlatformAdmin;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PlatformAdminMapper {

    /**
     * 根据账号查询管理员信息
     *
     * @param adminAccount
     * @return
     */
    @Select("select id, admin_account, admin_pwd, admin_type, status, created_at from platform_admin where admin_account=#{adminAcount}")
    PlatformAdmin getByAdminAccount(String adminAccount);

    /**
     * 根据id查询信息
     * @param id
     */
    @Select("select * from platform_admin where id=#{id}")
    PlatformAdmin selectById(Long id);

    /**
     * 添加管理员
     * @param platformAdmin
     */
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id") //主键回显
    @Insert("insert into platform_admin (admin_account, admin_pwd, admin_type, status, created_at)\n" +
            "values (#{adminAccount},#{adminPwd},#{adminType},#{status},#{createdAt})")
    void insert(PlatformAdmin platformAdmin);

    /***
     * 更新管理员信息
     * @param platformAdmin
     */
    void updateById(PlatformAdmin platformAdmin);

    /**
     * 根据ID删除
     * @param id
     */
    @Delete("delete from platform_admin where id=#{id}")
    void deleteById(Long id);

    List<PlatformAdmin> selectByPlatformAdmin(PlatformAdmin platformAdmin);
}
