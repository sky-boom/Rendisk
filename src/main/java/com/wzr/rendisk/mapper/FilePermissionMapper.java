package com.wzr.rendisk.mapper;

import com.wzr.rendisk.entity.FilePermission;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FilePermissionMapper {
    int deleteByPrimaryKey(Long id);

    int insert(FilePermission record);

    int insertSelective(FilePermission record);

    FilePermission selectByPrimaryKey(Long id);
}