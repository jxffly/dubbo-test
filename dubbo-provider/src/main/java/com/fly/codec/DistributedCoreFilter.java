package com.fly.codec;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.extension.SPI;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.dubbo.rpc.protocol.dubbo.DecodeableRpcInvocation;
import com.fly.codec.DistributedContextTools.DistributedParamManager.*;

import com.fly.codec.DistributedContextTools.DistributedParamManager.AdminId;
import com.fly.codec.DistributedContextTools.DistributedParamManager.Aid;
import com.fly.codec.DistributedContextTools.DistributedParamManager.Bid;
import com.fly.codec.DistributedContextTools.DistributedParamManager.DeviceId;
import com.fly.codec.DistributedContextTools.DistributedParamManager.DeviceType;
import com.fly.codec.DistributedContextTools.DistributedParamManager.KdtId;
import com.fly.codec.DistributedContextTools.DistributedParamManager.OpAdminId;
import com.fly.codec.DistributedContextTools.DistributedParamManager.OpAdminName;
import com.fly.codec.DistributedContextTools.DistributedParamManager.RequestIp;
import com.fly.codec.DistributedContextTools.DistributedParamManager.ShopId;
import com.fly.utils.JsonUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;


/**
 * 处理卡门中的通用参数信息，使用方式为，只要卡门传过来的参数类型是distributed.开头的参数都是系统处理范围，不过为了避免滥用，
 * 所以目前程序里面只处理admin_id、request_ip和kdt_id几个参数，后续需要增加什么参数只需要按照逻辑添加即可，如果在调用非体系内的服务时
 * ，需要在dubbo:reference标签里面增加filter="-kernel"的属性来去掉该过滤器，因为该过滤器在发起调用的时候会自动添加
 * 系统参数，这样会导致没有使用该过滤器的服务无法调通
 *
 * @author dft
 */
@Activate(group = {Constants.PROVIDER, Constants.CONSUMER}, order = -100000)
@SPI("kernel")
public class DistributedCoreFilter implements Filter {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DistributedCoreFilter.class);
    
    private static final String MDC_TRACE = "MDC_TRACE";
    
    private static final int CARMEN_SUCCESS_CODE = 200;
    
    private ThreadLocal<Stack<Integer>> stackLocal = ThreadLocal.withInitial(() -> new Stack<Integer>());
    
    
    public String getThrowableStr(Throwable e) {
        if (e == null) {
            return "";
        }
        
        ArrayWriter aw = new ArrayWriter();
        e.printStackTrace(aw);
        String[] arr = aw.toStringArray();
        if (arr == null) {
            return "";
        }
        
        StringBuilder strBuf = new StringBuilder();
        for (String anArr : arr) {
            strBuf.append(anArr).append("####");
        }
        return strBuf.toString();
    }
    
    
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        
        RpcInvocation inv = (RpcInvocation) invocation;
        String method = "";
        Result invoke = null;
        boolean isSuccess = true;
        long t = System.currentTimeMillis();
        
        // provider侧的调用处理
        if (inv instanceof DecodeableRpcInvocation || "true".equals(inv.getAttachment(Constants.GENERIC_KEY))) {
            inv.setAttachment(Constants.ASYNC_KEY, "false");
            
            try {
                // 设置请求的唯一key，方便日志的grep
                initLogMdc();
                
                // 处理通用invoke方式调用，目前是卡门调用过来的方式
                if (inv.getMethodName().equals(Constants.$INVOKE) && inv.getArguments() != null && inv.getArguments().length == 3 && !invoker.getUrl().getParameter(Constants.GENERIC_KEY, false)) {
                    boolean isCarmen = false;
                    try {
                        method = (String) inv.getArguments()[0];
                        String[] typesTmp = (String[]) inv.getArguments()[1];
                        Object[] argsTmp = (Object[]) inv.getArguments()[2];
                        List<String> types = new ArrayList<>();
                        List<Object> args = new ArrayList<>();
                        // 将系统级的分布式变量放到统一的分布式上下文里面，同时将他们从传入参数中去除
                        for (int i = 0; i < typesTmp.length; i++) {
                            if (DistributedContextTools.DistributedParamManager.isDistributedParam(typesTmp[i])) {
                                //http
                                Class<?> key = DistributedContextTools.DistributedParamManager.get(typesTmp[i]);
                                DistributedContextTools.set(key, argsTmp[i]);
                                continue;
                            }
                            types.add(typesTmp[i]);
                            args.add(argsTmp[i]);
                        }
                        isCarmen = putCarmenParamIntoContext(inv.getAttachments());
                        // 保存过滤掉系统参数后的结果
                        if(types.size() > 0 && args.size()>0){
                            inv.getArguments()[1] = types.toArray(new String[0]);
                            inv.getArguments()[2] = args.toArray();
                        }
                        
                        invoke = invoker.invoke(inv);
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("core filter,path:{}:methodName:{},inArgs:{}", inv.getAttachment("path"), method, inv.getMethodName(), JsonUtils.toJson(argsTmp));
                        }
                        
                        if (invoke.hasException()) {
                            isSuccess = false;
                        }
                    } catch (Throwable e) {
                        isSuccess = false;
                        invoke = new RpcResult(e);
                    }
                    return dealResult(invoke, inv, isCarmen);
                }else {// 处理普通的rpc调用，即使用dubbo客户端直接调用的场景
                    String adminId = inv.getAttachment(AdminId.class.getCanonicalName());
                    if (null != adminId) {
                        DistributedContextTools.setAttr(AdminId.class, Long.valueOf(adminId));
                    }
                    String requestIp = inv.getAttachment(RequestIp.class.getCanonicalName());
                    if (null != requestIp) {
                        DistributedContextTools.setAttr(RequestIp.class, requestIp);
                    }
                    String kdtId = inv.getAttachment(KdtId.class.getCanonicalName());
                    if (null != kdtId) {
                        DistributedContextTools.setAttr(KdtId.class, Long.valueOf(kdtId));
                    }
                    String deviceId = inv.getAttachment(DeviceId.class.getCanonicalName());
                    if (null != deviceId) {
                        DistributedContextTools.setAttr(DeviceId.class, deviceId);
                    }
                    String deviceType = inv.getAttachment(DeviceType.class.getCanonicalName());
                    if (null != deviceType) {
                        DistributedContextTools.setAttr(DeviceType.class, deviceType);
                    }
                    
                    String aid = inv.getAttachment(Aid.class.getCanonicalName());
                    if (aid != null) {
                        DistributedContextTools.set(Aid.class.getCanonicalName(), String.valueOf(aid));
                    }
                    String bid = inv.getAttachment(Bid.class.getCanonicalName());
                    if (bid != null) {
                        DistributedContextTools.set(Bid.class.getCanonicalName(), String.valueOf(bid));
                    }
                    String shopId = inv.getAttachment(ShopId.class.getCanonicalName());
                    if (shopId != null) {
                        DistributedContextTools.set(ShopId.class.getCanonicalName(), String.valueOf(shopId));
                    }
                    String opAdminId = inv.getAttachment(OpAdminId.class.getCanonicalName());
                    if (opAdminId != null) {
                        DistributedContextTools.set(OpAdminId.class.getCanonicalName(), String.valueOf(opAdminId));
                    }
                    String opAdminName = inv.getAttachment(OpAdminName.class.getCanonicalName());
                    if (opAdminName != null) {
                        DistributedContextTools.set(OpAdminName.class.getCanonicalName(), String.valueOf(opAdminName));
                    }
                    String appVersion = inv.getAttachment(DistributedContextTools.DistributedParamManager.AppVersion.class.getCanonicalName());
                    if (appVersion != null) {
                        DistributedContextTools.set(DistributedContextTools.DistributedParamManager.AppVersion.class.getCanonicalName(), String.valueOf(appVersion));
                    }
                    String noSession = inv.getAttachment(DistributedContextTools.DistributedParamManager.NoSession.class.getCanonicalName());
                    if (noSession != null) {
                        DistributedContextTools.set(DistributedContextTools.DistributedParamManager.NoSession.class.getCanonicalName(), String.valueOf(noSession));
                    }
                    String identity = inv.getAttachment(DistributedContextTools.DistributedParamManager.Identity.class.getCanonicalName());
                    if (identity != null) {
                        DistributedContextTools.set(DistributedContextTools.DistributedParamManager.Identity.class.getCanonicalName(), Integer.valueOf(identity));
                    }
                }
                invoke = invoker.invoke(inv);
                if (invoke.hasException()) {
                    isSuccess = false;
                }
                return invoke;
            } catch (Throwable e) {
                LOGGER.warn("normal rpc invoke fail", e);
                isSuccess = false;
                return new RpcResult(e);
                
            } finally {
                // 调用结束后要清理掉分布式上下文，不然会有内存泄露和脏数据
                DistributedContextTools.clear();
                LOGGER.info("p:|" + method + "|" + (System.currentTimeMillis() - t) + "|" + isSuccess);
                clearLogMdc();
            }
        }else {
            try {
                // 设置请求的唯一key，方便日志的grep
                initLogMdc();
                // 获取需要传递的平台参数
                Long adminId = DistributedContextTools.getAdminId();
                String requestIp = DistributedContextTools.getRequestIp();
                Long KdtId = DistributedContextTools.getKdtId();
                String deviceId = DistributedContextTools.getDeviceId();
                Long bid = DistributedContextTools.getBid();
                Integer aid = DistributedContextTools.getAId();
                Long shopId = DistributedContextTools.getShopId();
                String deviceType = DistributedContextTools.getDeviceType();
                Long opAdminId = DistributedContextTools.getOpAdminId();
                String opAdminName = DistributedContextTools.getOpAdminName();
                String appVersion = DistributedContextTools.getAppVersion();
                Integer noSession = DistributedContextTools.getNoSession();
                method = inv.getMethodName();
                
                if (null != adminId) {
                    inv.setAttachment(AdminId.class.getCanonicalName(), adminId + "");
                }
                if (null != requestIp) {
                    inv.setAttachment(RequestIp.class.getCanonicalName(), requestIp);
                }
                if (null != KdtId) {
                    inv.setAttachment(KdtId.class.getCanonicalName(), KdtId + "");
                }
                if (null != deviceId) {
                    inv.setAttachment(DeviceId.class.getCanonicalName(), deviceId);
                }
                if (null != deviceType) {
                    inv.setAttachment(DeviceType.class.getCanonicalName(), deviceType + "");
                }
                if (aid != null) {
                    inv.setAttachment(Aid.class.getCanonicalName(), aid.toString());
                }
                if (bid != null) {
                    inv.setAttachment(Bid.class.getCanonicalName(), bid.toString());
                }
                if (shopId != null) {
                    inv.setAttachment(ShopId.class.getCanonicalName(), shopId.toString());
                }
                if (opAdminId != null)
                    inv.setAttachment(OpAdminId.class.getCanonicalName(), opAdminId.toString());
                if (opAdminName != null)
                    inv.setAttachment(OpAdminName.class.getCanonicalName(), opAdminName);
                // app版本信息
                if (appVersion != null) {
                    inv.setAttachment(DistributedContextTools.DistributedParamManager.AppVersion.class.getCanonicalName(), appVersion);
                }
                
                if (noSession != null) {
                    inv.setAttachment(DistributedContextTools.DistributedParamManager.NoSession.class.getCanonicalName(), noSession.toString());
                }
                
                invoke = invoker.invoke(inv);
                if (invoke.hasException()) {
                    isSuccess = false;
                }
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("outArgs[{}]", JsonUtils.toJson(invoke.getValue()));
                }
                return invoke;
            } catch (Throwable e) {
                LOGGER.warn("consumer invoke fail", e);
                isSuccess = false;
                return new RpcResult(e);
            } finally {
                LOGGER.info("c:|" + method + "|" + (System.currentTimeMillis() - t) + "|" + isSuccess);
                clearLogMdc();
            }
        }
        
    }
    
    
    private boolean putCarmenParamIntoContext(Map<String, String> attachments) {
        String adminId = attachments.get("admin_id");
        String requestIp = attachments.get("request_ip");
        String kdtId = attachments.get("kdt_id");
        String bid = attachments.get("kdt_id");
        String shopId = attachments.get("shop_id");
        String opAdminId = attachments.get("admin_id");
        String clientId = attachments.get("client_id");
        if(!StringUtils.isEmpty(adminId)){
            DistributedContextTools.setAttr(AdminId.class, Long.valueOf(adminId));
        }
        if(!StringUtils.isEmpty(requestIp)){
            DistributedContextTools.setAttr(RequestIp.class, requestIp);
        }
        if(!StringUtils.isEmpty(kdtId)){
            DistributedContextTools.setAttr(KdtId.class, Long.valueOf(kdtId));
        }
        if(!StringUtils.isEmpty(bid)){
            DistributedContextTools.setAttr(Bid.class, Long.valueOf(bid));
        }
        if(!StringUtils.isEmpty(shopId)){
            DistributedContextTools.setAttr(ShopId.class, Long.valueOf(shopId));
        }
        if(!StringUtils.isEmpty(opAdminId)){
            DistributedContextTools.setAttr(OpAdminId.class, Long.valueOf(opAdminId));
        }
        if(!StringUtils.isEmpty(clientId)){
            DistributedContextTools.setAttr(ClientId.class, clientId);
            DistributedContextTools.setAttr(OpenApi.class, true);
            return true;
        }else {
            return false;
        }
    }
    
    
    /**
     * 设置请求的唯一key，方便日志的grep
     */
    private void initLogMdc() {
        if (isLogMdc()) {
            Stack<Integer> stack = stackLocal.get();
            if (stack.isEmpty()) {
                // 设置请求的唯一key，方便日志的grep
                MDC.put(MDC_TRACE, System.currentTimeMillis()+"");
            }
            
            stack.push(1);
        }
    }
    
    
    /**
     * 处理通用调用类型的返回对象结果，需要将返回对象包装成baseresponse对象
     */
    Result dealResult(Result invoke, Invocation invocation, boolean isCarmen) {
        // 统一处理返回值，一遍能够达到给卡门使用的要求，同时对于卡门接口就不在返回异常了，统一包装成错误消息
        RpcResult rpcResult = (RpcResult) invoke;
        BaseResponse<?> br = null;
        // 对于异常信息，统一进行包装
        if (invoke.hasException()) {

            br = new BaseResponse<>(88888, invoke.getException().getMessage(), invoke.getValue());

            LOGGER.warn("rpc invoke exception:{}", invoke.getException());
            // 变更处理后需要清空原有的异常信息
            rpcResult.setException(null);
            rpcResult.setValue(br);
        }else if (invoke.getValue() instanceof Map) {
            // 这种通用调用返回结果也会被转换成map形式，所以这里要进行进一步判断
            String invokeClass;
            int resultCode = 10000;
            if ("true".equals(invocation.getAttachment(CarmenCodec.CARMEN_CODEC))) {
                invokeClass = (String) ((Map) invoke.getValue()).remove("class");
            }else if (DistributedContextTools.getOpenApi()) {
                invokeClass = (String) ((Map) invoke.getValue()).remove("class");
                resultCode = CARMEN_SUCCESS_CODE;
            }else {
                invokeClass = (String) ((Map) invoke.getValue()).get("class");
            }
            
          if (!BaseResponse.class.getName().equals(invokeClass)) {
                //todo 此场景openapi还没有测试到
                br = new BaseResponse<>(resultCode, "SUCCESS", invoke.getValue());
                rpcResult.setValue(br);
            }else {
                final Object data = ((Map) invoke.getValue()).get("data");
                Integer code = (Integer) ((Map) invoke.getValue()).get("code");
                if (10000 == code && DistributedContextTools.getOpenApi()) {
                    code = CARMEN_SUCCESS_CODE;
                }
                br = new BaseResponse<>(code, (String) ((Map) invoke.getValue()).get("message"), data);
                rpcResult.setValue(br);
            }
            
        }else if (!(invoke.getValue() instanceof BaseResponse)) {
            br = new BaseResponse<>(10000, "SUCCESS", invoke.getValue());
            rpcResult.setValue(br);
        }
        
        if (isCarmen) {//如果是卡门调用，要把正确返回码换成200
            br = (BaseResponse) rpcResult.getValue();
            if (10000 == br.getCode()) {
                br.setCode(200);
            }
            rpcResult.setValue(br);
        }
        rpcResult.setAttachment(CarmenCodec.CARMEN_CODEC, invocation.getAttachment(CarmenCodec.CARMEN_CODEC));
        return invoke;
    }
    
    
    private void clearLogMdc() {
        if (isLogMdc()) {
            Stack<Integer> stack = stackLocal.get();
            stack.pop();
            if (stack.isEmpty()) {
                // 设置请求的唯一key，方便日志的grep
                MDC.remove(MDC_TRACE);
                stackLocal.remove();
            }
        }
    }
    
    
    private boolean isLogMdc() {
        return LOGGER.isInfoEnabled();
    }
    
}
