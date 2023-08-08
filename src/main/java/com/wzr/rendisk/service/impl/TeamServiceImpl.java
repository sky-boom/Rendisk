package com.wzr.rendisk.service.impl;

import com.wzr.rendisk.core.exception.GlobalException;
import com.wzr.rendisk.entity.TeamInfo;
import com.wzr.rendisk.mapper.TeamMapper;
import com.wzr.rendisk.service.ITeamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author wzr
 * @date 2023-07-23 16:19
 */
@Service
@Slf4j
public class TeamServiceImpl implements ITeamService {

    @Autowired
    private TeamMapper teamMapper;
    
    @Override
    public Long postTeam(TeamInfo teamInfo) {
        Long teamId = teamInfo.getId();
        // 无id是新增，有id是修改
        if (teamId == null) {
            int result = teamMapper.insert(teamInfo);
            if (result == 0) {
                log.error("数据库无插入记录，这肯定是bug! ");
                throw new GlobalException();
            }
            // 主键回填
            teamId = teamInfo.getId();
        } else {
            int result = teamMapper.updateSelective(teamInfo);
            if (result == 0) {
                log.error("数据库无更新记录，这肯定是bug! ");
                throw new GlobalException();
            }
        }
        return teamId;
    }

    @Override
    public boolean delete(TeamInfo teamInfo) {
        return false;
    }
}
