package com.wzr.rendisk.mapper;

import com.wzr.rendisk.entity.TeamInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TeamMapper {
    int deleteByPrimaryKey(Long id);

    int insert(TeamInfo record);

    int insertSelective(TeamInfo record);

    TeamInfo selectByPrimaryKey(Long id);
    
    int updateSelective(TeamInfo record);
    
}