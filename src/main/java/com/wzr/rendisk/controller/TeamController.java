package com.wzr.rendisk.controller;

import com.wzr.rendisk.core.result.GlobalResult;
import com.wzr.rendisk.core.result.ResultData;
import com.wzr.rendisk.entity.TeamInfo;
import com.wzr.rendisk.entity.User;
import com.wzr.rendisk.service.ITeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 团队接口
 * @author wzr
 * @date 2023-07-23 16:20
 */
@RestController
@RequestMapping("/api/v1/team")
public class TeamController {

    @Autowired
    private ITeamService teamService;
    
    /**
     * 增加/修改团队
     * @return id
     */
    @RequestMapping("/post")
    public ResultData<Long> post(User user, TeamInfo teamInfo) {
        teamInfo.setCaptainId(user.getId());
        return GlobalResult.success( teamService.postTeam(teamInfo) );
    }
    
    
}
