package com.wzr.rendisk.controller;

import com.wzr.rendisk.core.GlobalException;
import com.wzr.rendisk.core.GlobalResult;
import com.wzr.rendisk.core.ResultCode;
import com.wzr.rendisk.core.ResultData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * @author wzr
 * @date 2023-06-01 23:58
 */
@RestController
public class TestController {
    
    @GetMapping("/test")
    public ResultData<?> test() {
        List<Integer> list = Arrays.asList(1, 2, 3);
        return GlobalResult.success(list);
    }

    @GetMapping("/test2")
    public ResultData<?> test2() {
        List<Integer> list = Arrays.asList(1, 2, 3);
        if (true) {
            throw new GlobalException();
        }
        return GlobalResult.success(list);
    }

    @GetMapping("/test3")
    public ResultData<?> test3() {
        List<Integer> list = Arrays.asList(1, 2, 3);
        if (true) {
            throw new GlobalException(ResultCode.RESOURCE_NOT_FOUND);
        }
        return GlobalResult.success(list);
    }
}
