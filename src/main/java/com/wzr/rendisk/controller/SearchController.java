package com.wzr.rendisk.controller;

import com.wzr.rendisk.core.exception.GlobalException;
import com.wzr.rendisk.core.result.GlobalResult;
import com.wzr.rendisk.core.result.ResultData;
import com.wzr.rendisk.dto.SearchDto;
import com.wzr.rendisk.entity.User;
import com.wzr.rendisk.service.ISearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wzr
 * @date 2023-06-20 22:08
 */
@RestController
@RequestMapping("/api/v1/search")
public class SearchController {
    
    @Autowired
    private ISearchService searchService;
    
    @GetMapping
    public ResultData<?> search(User user, SearchDto searchDto) {
        if (StringUtils.isEmpty(searchDto.getKeyword()) ) {
            throw new GlobalException();
        }
        return GlobalResult.success( searchService.search(user, searchDto) );
    }
    
}
