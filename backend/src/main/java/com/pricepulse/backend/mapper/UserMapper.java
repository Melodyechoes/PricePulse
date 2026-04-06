package com.pricepulse.backend.mapper;

import com.pricepulse.backend.common.entity.User;
import com.pricepulse.backend.common.entity.UserProduct;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    /**
     * 插入用户
     */
    @Insert("INSERT INTO users(username, password, status, created_at, updated_at) " +
            "VALUES(#{username}, #{password}, #{status}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    /**
     * 根据ID查询用户
     */
    @Select("SELECT * FROM users WHERE id = #{id}")
    User selectById(Long id);

    /**
     * 根据用户名查询用户（新增）
     */
    @Select("SELECT * FROM users WHERE username = #{username}")
    User selectByUsername(String username);

    /**
     * 更新用户信息
     */
    @Update("UPDATE users SET username = #{username}, password = #{password}, " +
            "status = #{status}, updated_at = NOW() WHERE id = #{id}")
    int update(User user);

    /**
     * 删除用户
     */
    @Delete("DELETE FROM users WHERE id = #{id}")
    int deleteById(Long id);

    /**
     * 根据用户名更新状态
     */
    @Update("UPDATE users SET status = #{status}, updated_at = NOW() WHERE username = #{username}")
    int updateStatusByUsername(@Param("username") String username, @Param("status") Integer status);

    /**
     * 查询所有用户
     */
    @Select("SELECT * FROM users ORDER BY created_at DESC")
    List<User> selectAllUsers();

    /**
     * 更新密码
     */
    @Update("UPDATE users SET password = #{password}, updated_at = NOW() WHERE username = #{username}")
    int updatePassword(@Param("username") String username, @Param("password") String password);

    /**
     * 更新用户角色
     */
    @Update("UPDATE users SET role = #{role}, updated_at = NOW() WHERE id = #{id}")
    int updateRole(@Param("id") Long id, @Param("role") String role);

}
