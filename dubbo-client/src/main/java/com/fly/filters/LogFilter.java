package com.fly.filters;

import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;

import java.util.Arrays;

import lombok.extern.slf4j.Slf4j;


/**
 * Created by jinxiaofei.
 * Time 2018/3/22 下午5:44
 * Desc 文件描述
 */
@Slf4j
public class LogFilter implements Filter {

    public LogFilter() {
        log.info("the logFilter is install");
    }


    @Override

    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        log.info("<<<<<<<<<<<<<<<this is a dubbo log before method call\n,attachment:{},params:{}",invocation.getAttachments().put("hello","world"),Arrays.toString(invocation.getArguments()));

        Result result = invoker.invoke(invocation);
        log.info(">>>>>>>>>>>>>>>>this is a dubbo log after method call\n,attachment:{},result:{}", Arrays.toString(invocation.getArguments()),result.getValue());

        return result;
    }
}
