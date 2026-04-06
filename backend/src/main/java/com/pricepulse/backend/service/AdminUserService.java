package com.pricepulse.backend.service;

import com.pricepulse.backend.common.entity.User;
import com.pricepulse.backend.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员用户管理服务
 */
@Service
@Slf4j
public class AdminUserService {

    @Autowired
    private UserMapper userMapper;

    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 获取所有用户列表（分页）
     */
    public Map<String, Object> getAllUsers(Integer page, Integer pageSize) {
        log.info("获取用户列表，page={}, pageSize={}", page, pageSize);
        
        List<User> users = userMapper.selectAllUsers();
        
        // 简单分页
        int total = users.size();
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        
        if (fromIndex >= total) {
            Map<String, Object> result = new HashMap<>();
            result.put("list", List.of());
            result.put("total", 0);
            result.put("page", page);
            result.put("pageSize", pageSize);
            return result;
        }
        
        List<User> pageData = users.subList(fromIndex, toIndex);
        
        // 移除敏感信息
        pageData.forEach(user -> user.setPassword(null));
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", pageData);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        
        return result;
    }

    /**
     * 根据 ID 获取用户详情
     */
    public User getUserById(Long id) {
        log.info("获取用户详情，id={}", id);
        User user = userMapper.selectById(id);
        if (user != null) {
            user.setPassword(null); // 不返回密码
        }
        return user;
    }

    /**
     * 更新用户状态
     */
    @Transactional
    public void updateUserStatus(Long userId, Integer status) {
        log.info("更新用户状态，userId={}, status={}", userId, status);
        
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 不允许禁用管理员
        if ("ADMIN".equals(user.getRole()) && status == 0) {
            throw new RuntimeException("不能禁用管理员账户");
        }
        
        userMapper.updateStatusByUsername(user.getUsername(), status);
        log.info("用户 {} 状态已更新为 {}", userId, status);
    }

    /**
     * 重置用户密码
     */
    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        log.info("重置用户密码，userId={}", userId);
        
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        String encodedPassword = passwordEncoder.encode(newPassword);
        userMapper.updatePassword(user.getUsername(), encodedPassword);
        
        log.info("用户 {} 密码已重置", userId);
    }

    /**
     * 删除用户
     */
    @Transactional
    public void deleteUser(Long userId) {
        log.info("删除用户，userId={}", userId);
        
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 不允许删除管理员
        if ("ADMIN".equals(user.getRole())) {
            throw new RuntimeException("不能删除管理员账户");
        }
        
        userMapper.deleteById(userId);
        log.info("用户 {} 已删除", userId);
    }

    /**
     * 统计用户数量
     */
    public Map<String, Object> getUserStatistics() {
        log.info("获取用户统计数据");
        
        List<User> allUsers = userMapper.selectAllUsers();
        
        long totalUsers = allUsers.size();
        long activeUsers = allUsers.stream().filter(u -> u.getStatus() == 1).count();
        long adminUsers = allUsers.stream().filter(u -> "ADMIN".equals(u.getRole())).count();
        long normalUsers = totalUsers - adminUsers;
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("inactiveUsers", totalUsers - activeUsers);
        stats.put("adminUsers", adminUsers);
        stats.put("normalUsers", normalUsers);
        
        return stats;
    }
}
