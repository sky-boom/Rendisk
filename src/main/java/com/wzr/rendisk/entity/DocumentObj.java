package com.wzr.rendisk.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 用于存储在elasticsearch的文件对象
 * @author wzr
 * @date 2023-06-09 23:37
 */
@Data
public class DocumentObj implements Serializable, Cloneable {

    /** 标记ES文档的唯一id */
    private String id;

    /** 当前文件所属用户id */
    private Long userId;
    
    /** mysql中的文件id */
    private Long docId;
    
    /** 团队id */
    private Long teamId;

    /** 文件名字 */
    private String docName; 
    
    /** 文件类型 */
    private String docType;
    
    /** 存入时：base64，取出时：分词文本 */
    private String content;

    private static final long serialVersionUID = 1L;
    
    public DocumentObj() {}
    
    public DocumentObj(Long userId, Long docId, String docName, String docType) {
        this.userId = userId;
        this.docId = docId;
        this.docName = docName;
        this.docType = docType;
    }

    /**
     * Object类的clone()方法,就是简单的浅拷贝方法.
     * DocumentObj中没有引用变量,因此可以使用浅拷贝
     * @return DocumentObj的浅拷贝
     */
    @Override
    public DocumentObj clone() {
        try {
            return (DocumentObj) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
    
}
