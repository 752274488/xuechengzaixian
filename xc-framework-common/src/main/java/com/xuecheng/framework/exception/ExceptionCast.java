package com.xuecheng.framework.exception;

import com.xuecheng.framework.model.response.ResultCode;

/**
 * 统一异常返回类
 *
 **/
public class ExceptionCast {

    public static void cast(ResultCode resultCode){

        throw new CustomException(resultCode);
    }
}
