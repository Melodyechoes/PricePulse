package com.pricepulse.backend.mapper;

import com.pricepulse.backend.common.entity.User;
import com.pricepulse.backend.common.entity.UserProduct;
import org.apache.ibatis.annotations.*;

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


}
