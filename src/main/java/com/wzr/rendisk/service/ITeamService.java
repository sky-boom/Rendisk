package com.wzr.rendisk.service;

import com.wzr.rendisk.entity.TeamInfo;

/**
 * 团队
 * @author wzr
 * @date 2023-07-23 16:18
 */
public interface ITeamService {

    /**
     * 新增/修改团队
     * @param teamInfo
     * @return 新增/修改的团队id
     */
    Long postTeam(TeamInfo teamInfo);
    
    boolean delete(TeamInfo teamInfo);
}
