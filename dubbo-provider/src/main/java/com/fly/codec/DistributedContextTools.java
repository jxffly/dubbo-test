package com.fly.codec;

import com.fly.codec.DistributedContextTools.DistributedParamManager.*;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;


public class DistributedContextTools {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(DistributedContextTools.class);
    
    
    public static class DistributedParamManager {
        
        public static class AdminId extends DistributedParam<Long> {
            
            public static String getName() {
                return "distributed.admin_id";
            }
            
            
            public static String getCarmenName() {
                return "adminId";
            }
        }
        
        
        public static class Aid extends DistributedParam<Integer> {
            
            public static String getName() {
                return "distributed.app_id";
            }
        }
        
        
        public static class Bid extends DistributedParam<Long> {
            
            public static String getName() {
                return "distributed.bid";
            }
            public static String getCarmenName() {
                return "bid";
            }
        }
        
        
        public static class ShopId extends DistributedParam<Long> {
            
            public static String getName() {
                return "distributed.shop_id";
            }
        }
        
        
        public static abstract class DistributedParam<T> {
            
            public static String getName() {
                return "";
            }
        }
        
        
        public static class KdtId extends DistributedParam<Long> {
            
            public static String getName() {
                return "distributed.kdt_id";
            }
            
            
            public static String getCarmenName() {
                return "kdtId";
            }
        }
    
    
        public static class DeptId extends DistributedParam<Long> {
        
            public static String getName() {
                return "distributed.dept_id";
            }
    
            public static String getCarmenName() {
                return "deptId";
            }
        }
    
    
        public static class RequestIp extends DistributedParam<String> {
            
            public static String getName() {
                return "distributed.request_ip";
            }
            
            
            public static String getCarmenName() {
                return "requestIp";
            }
        }
        
        
        public static class ClientId extends DistributedParam<String> {
            
            public static String getName() {
                return "distributed.client_id";
            }
            
            
            public static String getCarmenName() {
                return "clientId";
            }
        }
        
        
        public static class OpenApi extends DistributedParam<Boolean> {
            
            public static String getName() {
                return "distributed.open_api";
            }
        }
        
        
        public static class ApiFormat extends DistributedParam<Boolean> {
            
            public static String getName() {
                return "distributed.api_format";
            }
        }
        
        
        public static class CarmenParam extends DistributedParam<Boolean> {
            
            public static String getName() {
                return "CarmenParam";
            }
        }
        
        
        public static class DeviceId extends DistributedParam<String> {
            
            public static String getName() {
                return "distributed.device_id";
            }
        }
        
        
        public static class DeviceType extends DistributedParam<String> {
            
            public static String getName() {
                return "distributed.device_type";
            }
        }
        
        
        public static class OpAdminId extends DistributedParam<Long> {
            
            public static String getName() {
                return "distributed.op_admin_id";
            }
        }
        
        
        public static class OpAdminName extends DistributedParam<String> {
            
            public static String getName() {
                return "distributed.op_admin_name";
            }
        }
        
        
        public static class AppVersion extends DistributedParam<String> {
            
            public static String getName() {
                return "distributed.app_version";
            }
        }
        
        
        public static class NoSession extends DistributedParam<Integer> {
            
            public static String getName() {
                return "distributed.no_session";
            }
        }
        
        
        public static class Identity extends DistributedParam<Integer> {
            
            public static String getName() {
                return "distributed.identity";
            }
        }
    
        public static class Sign extends DistributedParam<String> {
        
            public static String getName() {
                return "distributed.sign";
            }
        }
        
        
        private static Map<String, Class<?>> cache = new HashMap<>();
        
        static {
            // 放入卡门调用时传入的参数类型映射
            cache.put(AdminId.getName(), AdminId.class);
            cache.put(RequestIp.getName(), RequestIp.class);
            cache.put(KdtId.getName(), KdtId.class);
            cache.put(DeptId.getName(), DeptId.class);
            cache.put(DeviceId.getName(), DeviceId.class);
            cache.put(DeviceType.getName(), DeviceType.class);
            cache.put(Aid.getName(), Aid.class);
            cache.put(ShopId.getName(), ShopId.class);
            cache.put(Bid.getName(), Bid.class);
            cache.put(OpAdminId.getName(), OpAdminId.class);
            cache.put(OpAdminName.getName(), OpAdminName.class);
            cache.put(AppVersion.getName(), AppVersion.class);
            cache.put(NoSession.getName(), NoSession.class);
            cache.put(Identity.getName(), Identity.class);
            cache.put(ClientId.getName(), ClientId.class);
            cache.put(Sign.getName(), Sign.class);
            // 放入使用客户端直接调用时放入的参数类型
            cache.put(AdminId.class.getCanonicalName(), AdminId.class);
            cache.put(KdtId.class.getCanonicalName(), KdtId.class);
            cache.put(DeptId.class.getCanonicalName(), DeptId.class);
            cache.put(RequestIp.class.getCanonicalName(), RequestIp.class);
            cache.put(DeviceId.class.getCanonicalName(), DeviceId.class);
            cache.put(DeviceType.class.getCanonicalName(), DeviceType.class);
            cache.put(Aid.class.getCanonicalName(), Aid.class);
            cache.put(ShopId.class.getCanonicalName(), ShopId.class);
            cache.put(Bid.class.getCanonicalName(), Bid.class);
            cache.put(OpAdminId.class.getCanonicalName(), OpAdminId.class);
            cache.put(OpAdminName.class.getCanonicalName(), OpAdminName.class);
            cache.put(AppVersion.class.getCanonicalName(), AppVersion.class);
            cache.put(NoSession.class.getCanonicalName(), NoSession.class);
            cache.put(Identity.class.getCanonicalName(), Identity.class);
            cache.put(ClientId.class.getCanonicalName(), ClientId.class);
            cache.put(OpenApi.class.getCanonicalName(), OpenApi.class);
            cache.put(CarmenParam.class.getCanonicalName(), CarmenParam.class);
            cache.put(Sign.class.getCanonicalName(), Sign.class);
        }
        
        public static Class<?> get(Class<?> param) {
            return cache.get(param.getCanonicalName());
        }
        
        
        public static Class<?> get(String param) {
            return cache.get(param);
        }
        
        
        public static boolean isDistributedParam(Class<?> param) {
            return cache.containsKey(param.getCanonicalName());
        }
        
        
        public static boolean isDistributedParam(String param) {
          return cache.containsKey(param);
//            return "distributed".equals(param);
        }
    }
    
    
    private static DistributedContext context = new DistributedContext();
    
    
    /**
     * 由于每次调用都会重新放置context等对象.所以这里可以清除本线程所有的
     */
    public static void clear() {
        Long adminId = getAdminId();
        context.clear();
        LOGGER.trace("结束清除{}请求参数", adminId);
    }
    
    
    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        return (T) context.get(key);
    }
    
    
    public static Long getAdminId() {
        Object obj = get(AdminId.class.getCanonicalName());
        return getLong(obj);
    }


    public static Long getKdtId() {
        Object obj = get(KdtId.class.getCanonicalName());
        return getLong(obj);
    }

    public static Long getDeptId() {
        Object obj = get(DeptId.class.getCanonicalName());
        return getLong(obj);
    }

    public static Integer getNoSession() {
        Object obj = get(NoSession.class.getCanonicalName());
        if (obj != null) {
            if (obj instanceof Integer)
                return (Integer) obj;
            if (obj instanceof String) {
                return Integer.valueOf(obj.toString());
            }
        }
        return 0;
    }


    private static Long getLong(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof String) {
            if (StringUtils.isEmpty(obj.toString())) {
                return 0L;
            }
            return Long.valueOf(obj.toString());
        }else if (obj instanceof Number) {
            return Long.valueOf(obj + "");
        }
        return (Long) obj;
    }


    //获取应用id
    public static Integer getAId() {

        final Object aid = get(Aid.class.getCanonicalName());
        if (aid instanceof String) {
            return Integer.valueOf((String) aid);
        }else if (aid instanceof Long) {
            return Integer.valueOf(aid + "");
        }
        return (Integer) aid;
    }


    //获取应用id
    public static Long getShopId() {
        final Object shopId = get(ShopId.class.getCanonicalName());
        if (shopId instanceof String) {
            return Long.valueOf((String) shopId);
        }else if (shopId instanceof Integer) {
            return Long.valueOf(shopId + "");
        }
        return (Long) shopId;
    }

    //获取应用id


    /**
     * @deprecated 改个名字
     */
    @Deprecated
    public static Long getBId() {
        return getBid();
    }


    //获取应用id
    public static Long getBid() {
        final Object bid = get(Bid.class.getCanonicalName());
        if (bid == null)//bid为空尝试获取一下kdtId
            return getKdtId();
        if (bid instanceof String) {
            return Long.valueOf((String) bid);
        }
        return (Long) bid;

    }


    //获取操作人id
    public static Long getOpAdminId() {
        final Object opAdminId = get(OpAdminId.class.getCanonicalName());
        if (opAdminId instanceof String) {
            return Long.valueOf((String) opAdminId);
        }
        return (Long) opAdminId;
    }


    //获取操作人名字
    public static String getOpAdminName() {
        return get(OpAdminName.class.getCanonicalName());
    }


    // APP版本信息
    public static String getAppVersion() {
        return get(AppVersion.class.getCanonicalName());
    }


    public static String getRequestIp() {
        return get(RequestIp.class.getCanonicalName());
    }

    public static String getSign() {
        return get(DistributedParamManager.Sign.class.getCanonicalName());
    }


    public static String getClientId() {
        return get(DistributedParamManager.ClientId.class.getCanonicalName());
    }


    public static Boolean getOpenApi() {
        Object value = get(DistributedParamManager.OpenApi.class.getCanonicalName());
        if (value == null) {
            return false;
        }else {
            return (Boolean) value;
        }
    }


    public static Boolean getApiFormat() {
        Object value = get(DistributedParamManager.ApiFormat.class.getCanonicalName());
        if (value == null) {
            return false;
        }else {
            return (Boolean) value;
        }
    }


    public static String getDeviceId() {
        return get(DeviceId.class.getCanonicalName());
    }


    public static String getDeviceType() {
        return get(DeviceType.class.getCanonicalName());
    }


    //获取操作人identiy
    public static Integer getIdentity() {
        final Object identity = get(Identity.class.getCanonicalName());
        if (identity != null && identity instanceof String) {
            return Integer.valueOf((String) identity);
        }
        return (Integer) identity;
    }


    /**
     * 设置属性,有泛型检查
     */
    public static <T extends DistributedParam<V>, V> void setAttr(Class<T> key, V value) {
        context.put(key.getCanonicalName(), value);
    }
    
    
    @Deprecated
    public static <T> void set(Class<?> key, T value) {
        //        if (LOGGER.isDebugEnabled()) {
        //            LOGGER.debug("set distribution key:{},value:{}", key.getSimpleName(), value);
        //        }
        //        setAttr(key,value);
        context.put(key.getCanonicalName(), value);
    }
    
    
    public static <T> void set(String key, T value) {
        context.put(key, value);
    }
    
    public static <T> void setMap(Map<String,Object> distributedParam) {
        distributedParam.forEach((k,v)->{
            System.out.println(k + " = " + v);
            Class<?> key = DistributedParamManager.get(k);
            if(key != null){
                context.put(key.getCanonicalName(), v);
            }
        });
    }
    
    public static void main(String[] args) {
        System.out.println(Long.parseLong(""));
    }
    
}
  