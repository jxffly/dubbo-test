package com.fly.codec;

/**
 * Created by jinxiaofei.
 * Time 2018/3/23 上午10:29
 * Desc 文件描述
 */

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.SPI;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.Codec2;
import com.alibaba.dubbo.remoting.buffer.ChannelBuffer;
import com.alibaba.dubbo.remoting.exchange.Request;
import com.alibaba.dubbo.remoting.exchange.Response;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.dubbo.rpc.protocol.dubbo.DubboCountCodec;
import com.fly.codec.DistributedContextTools.DistributedParamManager;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fly.utils.JsonUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


@SPI("dubbo")
public class CarmenCodec implements Codec2 {

    public static final String CARMEN_CODEC = "CarmenCodec";

    private static final Logger LOGGER = LoggerFactory.getLogger(CarmenCodec.class);

    /**
     *
     */
    private static final byte[] CLRF = new byte[]{0x0D, 0x0A};

    private static final ObjectMapper om = new ObjectMapper();

    private static final String HB_URI = "_HB_";

    private static volatile byte[] HEATH_CHECK_RESP = null;

    static {
        HEATH_CHECK_RESP = encodeRPC(new BaseResponse<>(10000, "OK")).array();
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL);//默认不导出为空的字段
        om.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
    }

    private DubboCountCodec dubboCountCodec = new DubboCountCodec();




    private static ByteBuffer encodeRPC(BaseResponse response) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);

        try {
            // 1. Write HTTP status line.
            StringBuilder statusLine = new StringBuilder("HTTP/1.1 ");
            switch (response.getCode()) {
                case 99999: //服务内部错误->暂时修改为200,避免返回504异常
                    //                    statusLine.append("500 Internal Server Error");
                    statusLine.append("200 OK");
                    break;
                case 99996: //心跳检查失败 , ResponseCode.HEART_BEAT_FAILED
                    statusLine.append("404 Not Found");
                    break;
                default:
                    statusLine.append("200 OK");
                    break;
            }
            baos.write(statusLine.toString().getBytes("UTF-8"));
            baos.write(CLRF);
            baos.write("Access-Control-Allow-Origin:*".getBytes("UTF-8"));
            baos.write(CLRF);
            baos.write("Content-Language:zh-CN".getBytes("UTF-8"));
            baos.write(CLRF);
            baos.write("Content-Type:application/json;charset=utf-8".getBytes("UTF-8"));
            baos.write(CLRF);
            // buffer.put(statusLine.toString().getBytes());
            // buffer.put(CLRF);

            StringBuilder header = new StringBuilder();
            byte[] body = null;
            try {
                if (response.getData() != null) {
                    // 删除JSON 中的 「class」
                    JsonInclude annotation = response.getData().getClass().getAnnotation(JsonInclude.class);
                    if (annotation != null) {//如果含有注解,则按照注解导出
                        final ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.setSerializationInclusion(annotation.value());
                        body = ("{\"response\":" + objectMapper.writeValueAsString(response) + "}").getBytes("UTF-8");
                    }
                }

                if (body == null) {
                    body = ("{\"response\":" + om.writeValueAsString(response) + "}").getBytes("UTF-8");
                }
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("om write json:{}", om.writeValueAsString(response));
                }
            } catch (Throwable e) {
                LOGGER.warn("encodeRPC 出错,异常信息:{}", e);
            }

            header.setLength(0);
            header.append("Content-Length: ");
            header.append(null == body ? 0 : body.length);
            baos.write(header.toString().getBytes());
            baos.write(CLRF);

            baos.write(CLRF);

            if (null != body) {
                baos.write(body);
            }
            return ByteBuffer.wrap(baos.toByteArray());
        } catch (IOException e) {
            LOGGER.warn("encodeRPC 出错,异常信息:{}", e);
        }
        return null;
    }




    @Override
    public void encode(Channel channel, ChannelBuffer buffer, Object msg) throws IOException {
        //处理心跳
        if (msg instanceof Response && ((Response) msg).getStatus() == Response.BAD_REQUEST) {
            Response res = (Response) msg;

            if (res.getErrorMessage().contains("health_check")) {
                buffer.writeBytes(HEATH_CHECK_RESP);
                return;
            }
            if (res.getErrorMessage().contains("check")) {
                buffer.writeBytes(HEATH_CHECK_RESP);
                return;
            }
            if (res.getErrorMessage().contains("online")) { //上线
                HEATH_CHECK_RESP = encodeRPC(new BaseResponse<>(10000, "OK")).array();
                buffer.writeBytes(HEATH_CHECK_RESP);
                return;
            }
            if (res.getErrorMessage().contains("offline")) { //下线
                HEATH_CHECK_RESP = encodeRPC(new BaseResponse<>(9999, "NOT OK")).array();
                buffer.writeBytes(HEATH_CHECK_RESP);
                return;
            }
        }

        if (msg instanceof Response && ((Response) msg).getResult() instanceof RpcResult) {
            RpcResult res = (RpcResult) ((Response) msg).getResult();
            // 只处理卡门编解码
            String CarmenCodec = res.getAttachment(CARMEN_CODEC);
            if (res.getValue() instanceof BaseResponse && "true".equals(CarmenCodec)) {
                ByteBuffer encoder = encodeRPC((BaseResponse) res.getValue());
                assert encoder != null;
                buffer.writeBytes(encoder.array());
                return;
            }
        }
        dubboCountCodec.encode(channel, buffer, msg);
    }


    @Override
    public Object decode(Channel channel, ChannelBuffer buffer) throws IOException {

        Request req = new Request(-1);
        try {

            ByteBuffer buf = buffer.toByteBuffer();
            buf.mark();

            // 判断是否是http协议,如果不是就用原生的dubbo协议解析
            byte magic = buf.get();
            if (magic != 'G' && magic != 'P') {
                buf.reset();
                return dubboCountCodec.decode(channel, buffer);
            }

            // 下面解析完成后要组装成dubbo的相关对象
            buf.reset();
            String requestLine = null, headerLine = null;
            String strArray[] = null;
            boolean isPostMethod = true;
            String interfaceName = null;
            String methodName = null;
            String param = null;
            int contentLengthHead = -1;
            String version = null;
            String versionStr = null;

            while (true) {
                headerLine = readHttpLine(buf);

                if (headerLine == null) {
                    return DecodeResult.NEED_MORE_INPUT;
                }

                // 只有对于post方法才需要读取body
                if (headerLine.length() == 0 && isPostMethod) { // End of HTTP
                    // header
                    // Read HTTP body content
                    int contentLength = 0;

                    if (contentLengthHead != -1) {
                        contentLength = contentLengthHead;
                    }

                    if (contentLength != buf.remaining()) {
                        return DecodeResult.NEED_MORE_INPUT;
                    }

                    if (contentLength > 0) {
                        byte[] content = new byte[contentLength];
                        buf.get(content);
                        param = new String(content, "UTF-8");
                    }
                    break;
                }

                if (requestLine == null) // first line
                {
                    // The first line is HTTP status line. [GET /img/gs.gif
                    // HTTP/1.1]
                    requestLine = headerLine;
                    strArray = requestLine.split(" ");
                    if ("GET".equals(strArray[0].trim())) {
                        isPostMethod = false;
                    }
                    String url = strArray[1].trim();

                    // 解析参数信息
                    int pIndex = url.indexOf("/?");
                    if (pIndex != -1) {
                        param = url.substring(pIndex + 2);
                    }else if ((pIndex = url.indexOf('?')) != -1) {
                        param = url.substring(pIndex + 1);
                    }

                    // 解析方法信息
                    int mIndex;
                    if (pIndex != -1) {
                        mIndex = url.lastIndexOf("/", pIndex - 1);
                        methodName = url.substring(mIndex + 1, pIndex);
                    }else {
                        mIndex = url.lastIndexOf('/');
                        methodName = url.substring(mIndex + 1);
                    }
                    // 解析版本信息
                    int vIndex = url.lastIndexOf('/', mIndex - 1);
                    //version = url.substring(vIndex + 1, mIndex);
                    if (vIndex != -1) {
                        // 解析接口信息
                        int iIndex = url.lastIndexOf('/', vIndex - 1);
                        interfaceName = "com.fly." + url.substring(iIndex + 1, vIndex);
                    }else { // 服务上下线URI 中没有 接口、版本
                        if (Objects.equals(methodName, HB_URI)) {
                            interfaceName = methodName;
                        }
                    }
                }else
                // header line
                {
                    // 获取app版本信息
                    if (StringUtils.isEmpty(versionStr)) {
                        boolean hasVersion = headerLine.indexOf("versionStr") != -1;
                        if (hasVersion) {
                            String[] versionArr = headerLine.split(" ");
                            if (null != versionArr && versionArr.length == 2) {
                                versionStr = versionArr[1];
                            }

                        }
                    }
                    int index = headerLine.indexOf(":");
                    if (index < 0) {
                        break;
                    }
                    if (index >= headerLine.length() - 1) {
                        if ("Content-Length".equals(headerLine.substring(0, headerLine.length() - 1).trim())) {
                            contentLengthHead = 0;
                        }
                    }else {
                        if ("Content-Length".equals(headerLine.substring(0, index).trim())) {
                            contentLengthHead = Integer.valueOf(headerLine.substring(index + 1).trim());
                        }
                    }
                }
            }
            Map<String, String> parseQueryString = parseQueryString(param);

            req.setVersion("2.0.0");
            req.setTwoWay(true);
            RpcInvocation inv = new RpcInvocation();

            // 解析协议参数
            String jsonValue = parseQueryString.get("json");
            if (null == jsonValue || "".equals(jsonValue.trim())) {
                jsonValue = "[]";
            }

            // 解析公共参数
            String adminId = parseQueryString.get("admin_id");
            if (null == adminId || "".equals(adminId.trim())) {
                adminId = parseQueryString.get("access_token");
            }
            String requestIp = parseQueryString.get("request_ip");
            if (null == requestIp || "".equals(requestIp.trim())) {
                requestIp = channel.getRemoteAddress().getAddress().getHostAddress();
            }
            String kdtId = parseQueryString.get("kdt_id");
            String deviceId = parseQueryString.get("device_id");
            String deviceType = parseQueryString.get("device_type");
            String aid = parseQueryString.get("aid");
            String bid = parseQueryString.get("bid");
            String shopId = parseQueryString.get("shop_id");

            String opAdminId = parseQueryString.get("op_admin_id");
            String opAdminName = parseQueryString.get("op_admin_name");
            String noSession = parseQueryString.get("no_session");
            String sign = parseQueryString.get("sign");

            inv.setArguments(new Object[]{methodName, new String[]{DistributedParamManager.AdminId.getName(), DistributedParamManager.RequestIp.getName(), DistributedParamManager.KdtId.getName(),
                    DistributedParamManager.DeviceId.getName(), DistributedParamManager.DeviceType.getName(), DistributedParamManager.Aid.getName(), DistributedParamManager.Bid.getName(),
                    DistributedParamManager.ShopId.getName(), DistributedParamManager.OpAdminId.getName(), DistributedParamManager.OpAdminName.getName(), DistributedParamManager.AppVersion.getName
                    (), DistributedParamManager.NoSession.getName(),DistributedParamManager.Sign.getName(), "json"}, new Object[]{adminId, requestIp, kdtId, deviceId, deviceType, aid, bid, shopId, opAdminId, opAdminName, versionStr,
                    noSession,sign, jsonValue}});
            inv.setMethodName(Constants.$INVOKE);
            inv.setParameterTypes(new Class[]{String.class, String[].class, Object[].class});
            Map<String, String> attachments = new HashMap<>();
            attachments.put(Constants.DUBBO_VERSION_KEY, "2.8.4");
            attachments.put(Constants.PATH_KEY, interfaceName);
            attachments.put(Constants.INTERFACE_KEY, interfaceName);
            // attachments.put("version", version);
            attachments.put(Constants.GENERIC_KEY, "true");
            attachments.put(Constants.ASYNC_KEY, "true");
            attachments.put(CARMEN_CODEC, "true");
            inv.setAttachments(attachments);
            buffer.skipBytes(buf.position());
            req.setData(inv);

            if (Objects.equals(interfaceName, "HeathCheck")) {
                req.setData("OK");
                req.setBroken(true);
            }else if (Objects.equals(interfaceName, HB_URI)) {
                String service = parseQueryString.get("service"); //服务上下线,值为online/offline
                if (service == null || service.trim().equals("")) {
                    service = "check"; //标志为健康检查
                }
                req.setData(service);
                req.setBroken(true);
            }
        } catch (Throwable t) {
            // bad request
            req.setBroken(true);
            req.setData(t);
            LOGGER.warn("decode 异常:{}", t);
        }
        return req;

    }


    private static String readHttpLine(ByteBuffer in) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        boolean isLineEnd = false;
        byte b;

        while (in.remaining() > 0) {
            if ((b = in.get()) == '\r') {
                in.get(); // skip '\n'
                isLineEnd = true;
                break;
            }else {
                baos.write(b);
            }
        }

        if (!isLineEnd) {
            return null;
        }

        return new String(baos.toByteArray());
    }


    private Map<String, String> parseQueryString(String src) {
        String key = null, value = null;
        String strArray[] = null;
        Map<String, String> parameters = new HashMap<>();
        if (src != null && src.length() != 0) {
            strArray = src.split("&");
            for (String pair : strArray) {
                int eqIndex = pair.indexOf("=");
                if (eqIndex > 0) {
                    try {
                        key = URLDecoder.decode(pair.substring(0, eqIndex), "UTF-8");
                        value = URLDecoder.decode(pair.substring(eqIndex + 1), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        LOGGER.warn("UnsupportedEncodingException:" + "UTF-8" + ", param=" + src);
                    }
                    parameters.put(key, value);
                }
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("parse parameter results:{}", JsonUtils.toJson(parameters));
        }
        return parameters;
    }

}

