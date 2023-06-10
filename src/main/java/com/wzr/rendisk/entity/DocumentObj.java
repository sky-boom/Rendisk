package com.wzr.rendisk.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 用于存储在elasticsearch的文件对象
 * @author wzr
 * @date 2023-06-09 23:37
 */
@Data
public class DocumentObj implements Serializable {

    /** 标记ES文档的唯一id */
    private String id;

    /** 当前文件所属用户id */
    private Long userId;
    
    /** mysql中的文件id */
    private Long docId;

    /** 文件名字 */
    private String docName; 
    
    /** 文件类型 */
    private String docType;
    
    /** 文件的base64内容 */
    private String content;

    private static final long serialVersionUID = 1L;
    
    public DocumentObj() {}
    
    public DocumentObj(Long userId, Long docId, String docName, String docType) {
        this.userId = userId;
        this.docId = docId;
        this.docName = docName;
        this.docType = docType;
    }
    
}
