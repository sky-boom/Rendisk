package com.wzr.rendisk.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Deprecated
@Data
public class FileInfo implements Serializable {
    private Long id;

    private Long parentId;

    private String fileName;

    private String realPath;

    private String virtualPath;

    private Long fileSize;

    private Long userId;

    private Date createTime;

    private Date updateTime;

    /** 冗余字段 */
    private String nickname;

    private static final long serialVersionUID = 1L;

    
}