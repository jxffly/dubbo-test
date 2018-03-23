package com.fly.codec;

import java.io.Serializable;

import lombok.Data;


/**
 * Created by jinxiaofei.
 * Time 2018/3/23 上午10:30
 * Desc 文件描述
 */
@Data
public class BaseResponse<T> implements Serializable {

    private Integer code;

    private String msg;

    private T data;


    public BaseResponse(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
    public BaseResponse(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
        this.data = null;
    }
}
