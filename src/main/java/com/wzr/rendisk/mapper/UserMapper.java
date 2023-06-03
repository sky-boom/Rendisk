package com.wzr.rendisk.mapper;

import com.wzr.rendisk.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表
 * @author wzr
 * @date 2023-6-2
 */
@Mapper
public interface UserMapper {

    /**
     * 根据用户id删除用户（一般用不上）
     * @param id 用户id
     * @return 响应行数
     */
    int deleteById(Long id);

    /**
     * 插入一个用户
     * @param record 用户
     * @return 响应行数
     */
    int insertUser(User record);

    /**
     * 根据用户名查询一个用户
     * @param username 用户名
     * @return 用户对象
     */
    User selectByUsername(String username);
}