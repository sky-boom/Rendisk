package com.wzr.rendisk.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 面包屑导航对象
 * @author wzr
 * @date 2023-06-07 11:12
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class BreadcrumbDto {

    /**
     * 当前目录名字
     */
    String currPathName;

    /**
     * 当前目录的虚拟目录
     */
    String currVirtPath;

    public BreadcrumbDto(String currPathName, String currVirtPath) {
        this.currPathName = currPathName;
        this.currVirtPath = currVirtPath;
    }
    
}
