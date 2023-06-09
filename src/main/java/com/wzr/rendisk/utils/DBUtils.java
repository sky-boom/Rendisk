package com.wzr.rendisk.utils;

import com.wzr.rendisk.core.exception.GlobalException;
import com.wzr.rendisk.core.result.ResultCode;
import lombok.extern.slf4j.Slf4j;

/**
 * 主要用于检查数据库操作是否符合规定
 * 如果不符合，则抛出相应异常。
 * 
 * @author wzr
 * @date 2023-06-08 18:18
 */
@Slf4j
public class DBUtils {

    /**
     * 检查数据库增加、更新、删除操作的
     * @param affectRows insert、update、delete的响应行数
     */
    public static void checkOperation(int affectRows) {
        if (affectRows == 0) {
            log.error("数据库操作成功的记录数为0，请检查操作参数是否存在问题。");
            throw new GlobalException(ResultCode.ERROR);
        }
    }
}
