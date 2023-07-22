package com.wzr.rendisk.dto;

import lombok.Data;

/**
 * @author wzr
 * @date 2023-06-20 22:10
 */
@Data
public class SearchDto {

    /**
     * 团队id
     */
    private Long teamId;

    /**
     * 搜索范围
     */
    private Integer scope;

    /**
     * 关键词
     */
    private String keyword;
    
}
