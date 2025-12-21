package com.liu.springbootdemo.mapper;


import com.liu.springbootdemo.POJO.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {
    
    /** 增
     * 插入一个新用户
     * @param user 新用户对象
     * @return 影响的行数，1为成功
     */
    @Insert("INSERT INTO users(username, password, email, create_time, last_login_time)" +
            "VALUES(#{username}, #{password}, #{email}, NOW(), NOW())")
    int insert(User user);


    /** 改
     * 根据用户名查询并修改用户密码
     * @param username 用户名
     * @return 影响的行数，1为成功
     */
    @Update("UPDATE users SET password = #{newPassword} WHERE username = #{username}")
    int updatePasswordByUsername(@Param("username") String username, @Param("newPassword") String newPassword);
    /**
     * 改
     * 根据用户名查询并修改最新登录时间
     * @param username 用户名
     * @return 影响的行数，1为成功
     */
    @Update("UPDATE users SET last_login_time = NOW() WHERE username = #{username}")
    int updateLogintimeByUsername(@Param("username") String username);


    /** 查
     * 根据用户名查询用户
     * @param username 用户名
     * @return 查找到的用户对象，如果不存在则返回null
     */
    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(String username);
    /** 查
     *  获取所有用户(带未加密密码)
     *  @return userlist 用户列表
     */
    @Select("SELECT * FROM users ")
    List<User> getAll();
    /**
     * 查
     * 根据邮箱查询用户
     * @param email 用户邮箱
     * @return 查找到的用户对象，如果不存在则返回null
     */
    @Select("SELECT * FROM users WHERE email = #{email}")
    User findByEmail(String email);
    /**
     * 查
     * 根据用户ID查询用户
     * @param id 用户ID
     * @return 查找到的用户对象，如果不存在则返回null
     */
    @Select("SELECT * FROM users WHERE id = #{id}")
    User findById(Long id);

    /** 删
     * 根据id查询并删除一个用户
     * @param id 用户名
     * @return 影响的行数，1为成功
     */
    @Delete("DELETE FROM users WHERE id = #{id}")
    int deleteById(Long id);

}
