package com.wzr.rendisk.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户表
 * @author wzr
 * @date 2023-6-2
 */
@Data
public class User implements Serializable {
    private Long id;

    private String nickname;

    private String username;

    private String password;

    private String avatarUrl;

    private Date createTime;

    private Date lastLoginTime;

    private static final long serialVersionUID = 1L;


}