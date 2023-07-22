package com.wzr.rendisk.service;

import com.wzr.rendisk.dto.SearchDto;
import com.wzr.rendisk.entity.DocumentObj;
import com.wzr.rendisk.entity.User;

import java.util.List;

/**
 * 搜索功能相关
 * @author wzr
 * @date 2023-06-09 23:57
 */
public interface ISearchService {

    /**
     * 当用户登录后，将用户所有文件加载到elasticsearch中。
     * @param user 用户
     */
    List<DocumentObj> loadAllFile(User user);

    /**
     * 当用户注销后，将用户在es中的所有文件都删除
     * @param user 用户
     */
    void removeAllFile(User user);

    /**
     * 根据关键词，搜索文档
     * @param user
     * @param searchDto
     * @return
     */
    List<DocumentObj> search(User user, SearchDto searchDto);

    /**
     * （测试）根据关键词，搜索文档
     * @param keyword
     * @return
     */
    List<DocumentObj> testSearch(String keyword);

    /**
     * （测试）把本地文档加载到elasticsearch中
     */
    boolean testLoadDocument();
}
