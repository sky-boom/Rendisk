package com.wzr.rendisk.dto;

import lombok.Data;

import java.util.Date;

/**
 * 返回用户基本信息，不涉及敏感信息（如密码、用户id等）
 * @author wzr
 * @date 2023-06-06 11:24
 */
@Data
public class UserDto {
    
    private String nickname;

    private String username;

    private String avatarUrl;

    private Date lastLoginTime;
}
