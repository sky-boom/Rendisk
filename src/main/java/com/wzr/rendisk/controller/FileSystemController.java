package com.wzr.rendisk.controller;

import com.wzr.rendisk.service.IFileSystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件系统接口
 * @author wzr
 * @date 2023-06-07 20:25
 */
@RestController
@RequestMapping("/api/v1/fs")
public class FileSystemController {
    
    @Autowired
    private IFileSystemService fileSystemService;
    
//    /**
//     * 【创建目录】
//     * @param user 当前登录的用户(已配置mvc参数)
//     * @param addFolderVo (parentPath, addName)前端文件夹对象
//     * @return
//     */
//    @RequestMapping("/folder/mkdir")
//    public ResponseEntity<RespBean> mkdir(User user, AddFolderVo addFolderVo) {
//        // 1.名字为空，或包含特殊字符，则提示错误
//        if (addFolderVo.getAddName() == null || addFolderVo.getAddName().matches(Cloudisk.NAME_EXCEPT_REGES)) {
//            throw new GlobalException(RespBeanEnum.CLOUD_FOLDER_RENAME_ERR);
//        }
//        // 2.获取新增的虚拟路径(若已存在则已抛异常)
//        String virtualPath = getAddVirtPath(user.getId(), addFolderVo.getParentPath(), addFolderVo.getAddName(), Cloudisk.FOLDER_TYPE_CODE);
//        addFolderVo.setCurrVirtPath(virtualPath);
//        // 3.开始向mysql和hdfs插入数据
//        fileService.mkdir(user.getId(), addFolderVo);
//        // 无异常时，即返回成功。
//        return ResponseEntity.ok(RespBean.success());
//    }
}
