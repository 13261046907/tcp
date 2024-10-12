package com.tcp;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.config.RedisUtil;
import com.mqtt.MQTTConnect;
import com.rk.config.WebConfig;
import com.rk.domain.DataPackage;
import com.rk.utils.CacheManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class NettyTcpServerHandler extends ChannelInboundHandlerAdapter {
    private final RedisUtil redisUtil;

    private final MQTTConnect mqttConnect;

    // 构造函数注入RedisUtil
    public NettyTcpServerHandler(RedisUtil redisUtil, MQTTConnect mqttConnect) {
        this.redisUtil = redisUtil;
        this.mqttConnect = mqttConnect;
    }

    /**
     * 管理一个全局map，保存连接进服务端的通道数量
     */
    private static final ConcurrentHashMap<String, ChannelHandlerContext> CHANNEL_MAP = new ConcurrentHashMap<>();

    /**
     * @param ctx
     * @author xiongchuan on 2019/4/28 16:10
     * @DESCRIPTION: 有客户端连接服务器会触发此函数
     * @return: void
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {

        InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();

        String clientIp = insocket.getAddress().getHostAddress();
        int clientPort = insocket.getPort();

        //获取连接通道唯一标识
        ChannelId channelId = ctx.channel().id();

        System.out.println();
        //如果map中不包含此连接，就保存连接
        if (CHANNEL_MAP.containsKey(channelId)) {
            log.info("客户端【" + channelId + "】是连接状态，连接通道数量: " + CHANNEL_MAP.size());
        } else {
            //保存连接
            CHANNEL_MAP.put(channelId+"", ctx);

            log.info("客户端【" + channelId + "】连接netty服务器[IP:" + clientIp + "--->PORT:" + clientPort + "]");
            log.info("连接通道数量: " + CHANNEL_MAP.size());
        }
    }

    /**
     * @param ctx
     * @author xiongchuan on 2019/4/28 16:10
     * @DESCRIPTION: 有客户端终止连接服务器会触发此函数
     * @return: void
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {

        InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();

        String clientIp = insocket.getAddress().getHostAddress();

        ChannelId channelId = ctx.channel().id();

        //包含此客户端才去删除
        if (CHANNEL_MAP.containsKey(channelId)) {
            //删除连接
            CHANNEL_MAP.remove(channelId);

            System.out.println();
            log.info("客户端【" + channelId + "】退出netty服务器[IP:" + clientIp + "--->PORT:" + insocket.getPort() + "]");
            log.info("连接通道数量: " + CHANNEL_MAP.size());
        }
        log.info("下线或者强制退出时触发：" + ctx.channel().remoteAddress());
        ctx.close();
    }

   /* *//**
     * @param ctx
     * @author xiongchuan on 2019/4/28 16:10
     * @DESCRIPTION: 有客户端发消息会触发此函数
     * @return: void
     *//*
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String hex=ByteBufUtil.hexDump(((String) msg).getBytes());
        log.info("加载客户端报文......");
        log.info("【" + ctx.channel().id() + "】" + " :" + hex);

        *//**
         *  下面可以解析数据，保存数据，生成返回报文，将需要返回报文写入write函数
         *
         *//*
        //响应客户端
        ctx.write("I got server message thanks server!");
    }*/
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            String channelId = ctx.channel().id() + "";
            String hex=  msg.toString().trim();
            log.info("加载客户端报文......");
            log.info("【" + ctx.channel().id() + "】" + " :" + hex);
            DataPackage dp = DataPackage.from(msg.toString().trim());
            if (null == dp) {
                log.info("接收原始数据1:{}: " + hex);
                Object instruction = redisUtil.get(channelId);
                if(!Objects.isNull(instruction)) {
                    log.info("redisKey:{},instruction:{}",channelId,instruction);
                    //响应客户端
                    ctx.write(instruction);
                    // 输出结果
                    List<String> hexList = getHexList(hex, 4);
                    log.info("hexList:{}", JSONObject.toJSONString(hexList));
                    log.info("channelWrite=channelId:{},msg:{}",channelId,msg);
                    Map<String, Object> propertiesMap = new HashMap<>();
                    propertiesMap.put("111111", hexList.get(0));
                    syncSendMessageToDevice("111111", instruction+"", propertiesMap);
                    //属性设备
                 /*   try{
                        Mono<DeviceDetail> deviceDetail = service.getDeviceDetail(instruction);
                        DeviceDetail detail = deviceDetail.block();
                        if (!Objects.isNull(detail)) {
                            log.info("DeviceDetail:{}", JSONObject.toJSONString(detail));
                            String metadata = detail.getMetadata();
                            String productId = detail.getProductId();
                            List<ProductProperties> propertiesList = new ArrayList<>();
                            if (StringUtils.isNotBlank(metadata)) {
                                JSONObject metadataJson = JSONObject.parseObject(metadata);
                                propertiesList = JSONArray.parseArray(metadataJson.getString("properties"), ProductProperties.class);
                            }
                            if (!CollectionUtils.isEmpty(hexList) && !CollectionUtils.isEmpty(propertiesList)) {
                                for (int i = 0; i < propertiesList.size(); i++) { // Adjust t
                                    Map<String, Object> propertiesMap = new HashMap<>();
                                    ProductProperties productProperties = propertiesList.get(i);
                                    propertiesMap.put(productProperties.getId(), hexList.get(i));
                                    log.info("deviceId:{},param:{}", instruction, JSONObject.toJSONString(propertiesMap));
                                    syncSendMessageToDevice(productId, instruction, propertiesMap);
                                }
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }*/
                }
//                ctx.close();
                return;
            }

            log.info("接收数据2: " + dp.getMN() + "\t" + msg.toString() + "\r\n");
            if (!dp.getCN().equalsIgnoreCase("login") && (null == dp.getData() || dp.getMN() == null)) {
                log.info("接收原始数据3: " + msg.toString().trim());
//                ctx.close();
                return;
            }

            CacheManager.getInstance().updateDevice(dp.getMN(), ctx);
            boolean boo = true;
            String url = null;
            Map<String, Object> data = new HashMap();
            String var7 = dp.getCN().toLowerCase();
            byte var8 = -1;
            switch(var7.hashCode()) {
                case 113250:
                    if (var7.equals("rtd")) {
                        var8 = 1;
                    }
                    break;
                case 3064427:
                    if (var7.equals("ctrl")) {
                        var8 = 2;
                    }
                    break;
                case 103149417:
                    if (var7.equals("login")) {
                        var8 = 0;
                    }
                    break;
                case 110621352:
                    if (var7.equals("trans")) {
                        var8 = 4;
                    }
                    break;
                case 1082290915:
                    if (var7.equals("receive")) {
                        var8 = 3;
                    }
            }

            List convertToList;
            switch(var8) {
                case 0:
                    url = WebConfig.getLogin();
                    data.put("type", dp.getCN());
                    data.put("deviceAddr", dp.getMN());
                    break;
                case 1:
                    url = WebConfig.getRealTimeData();
                    data.put("type", dp.getCN());
                    data.put("deviceAddr", dp.getMN());
                    data.put("data", dp.getData());
                    break;
                case 2:
                    boo = false;
                    url = WebConfig.getCtrl();
                    convertToList = DataPackage.convertToList(dp.getData());
                    data.put("type", dp.getCN());
                    data.put("deviceAddr", dp.getMN());
                    data.put("data", convertToList);
                    break;
                case 3:
                    boo = false;
                    url = WebConfig.getReceive();
                    convertToList = DataPackage.convertToList(dp.getData());
                    data.put("type", dp.getCN());
                    data.put("deviceAddr", dp.getMN());
                    data.put("data", convertToList);
                    break;
                case 4:
                    boo = false;
                    url = WebConfig.getTrans();
                    convertToList = DataPackage.convertToList(dp.getData());
                    data.put("type", dp.getCN());
                    data.put("deviceAddr", dp.getMN());
                    data.put("data", convertToList);
            }

            if (boo) {
                ctx.writeAndFlush(dp.toAck());
            }
            this.post(url, JSONUtil.toJsonStr(data));
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            ReferenceCountUtil.release(msg);
        }
    }

    public List<String> getHexList(String convertedHexString, int num){
        int startIndex = 6; // Starting index for the first humidity
        List<String> hexList = new ArrayList<>();
        int length = 4; // Length of each humidity substring
        for (int i = 0; i <= num; i++) { // Adjust the loop count based on how many substrings you want
            String hex= convertedHexString.substring(startIndex + (i * length), startIndex + ((i + 1) * length));
            String hexStr = hexToStr(hex);
            hexList.add(hexStr);
        }
        return  hexList;
    }

    private String hexToStr(String hexValue){
        int decValue = Integer.parseInt(hexValue, 16);
        double dividedByTen = (double) decValue / 10.0;
        DecimalFormat df = new DecimalFormat("0.00");
        String result = df.format(dividedByTen);
        return result;
    }

    public void post(String finalUrl, String params) {
        (new Thread(new Runnable() {
            public void run() {
                try {
                    if (finalUrl != null) {
                        String body = params.replace("-", ":");
                        String data = HttpRequest.post(finalUrl).body(body, "application/json").execute().body();
                        log.info("推送地址: " + finalUrl + "\t" + data);
                    }
                } catch (Exception var3) {
                    log.info(finalUrl, var3);
                }

            }
        })).start();
    }

    /**
     * 从服务端收到新的数据、读取完成时调用
     *
     * @param ctx
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws IOException
    {
        System.out.println("channelReadComplete");
        ctx.flush();
    }

    /**
     * @param msg        需要发送的消息内容
     * @param channelId 连接通道唯一id
     * @author xiongchuan on 2019/4/28 16:10
     * @DESCRIPTION: 服务端给客户端发送消息
     * @return: void
     */
    public void channelWrite(String channelId, String msg) throws Exception {
        ChannelHandlerContext ctx = CHANNEL_MAP.get(channelId);
        try{
            if (ctx == null) {
                log.info("通道【" + channelId + "】不存在");
                return;
            }
            if (msg == null && msg == "") {
                log.info("服务端响应空的消息");
                return;
            }
            redisUtil.set(channelId,msg,60*60);
            //将客户端的信息直接返回写入ctx
            ByteBuf bufAck = ctx.alloc().buffer();
            byte[] payload = hexStringToByteArray(msg);
            bufAck.writeBytes(payload);
            ctx.writeAndFlush(bufAck);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public byte[] hexStringToByteArray(String hexString) {
        int length = hexString.length();
        byte[] byteArray = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            byteArray[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i+1), 16));
        }
        return byteArray;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        String socketString = ctx.channel().remoteAddress().toString();

        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                log.info("Client: " + socketString + " READER_IDLE 读超时");
                ctx.disconnect();
            } else if (event.state() == IdleState.WRITER_IDLE) {
                log.info("Client: " + socketString + " WRITER_IDLE 写超时");
                ctx.disconnect();
            } else if (event.state() == IdleState.ALL_IDLE) {
                log.info("Client: " + socketString + " ALL_IDLE 总超时");
                ctx.disconnect();
            }
        }
    }

    /**
     * @param ctx
     * @author xiongchuan on 2019/4/28 16:10
     * @DESCRIPTION: 发生异常会触发此函数
     * @return: void
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

        System.out.println();
        ctx.close();
        log.info(ctx.channel().id() + " 发生了错误,此连接被关闭" + "此时连通数量: " + CHANNEL_MAP.size());
        //cause.printStackTrace();
    }

    private void syncSendMessageToDevice(String productId,String deviceId,Map<String, Object> props){
        this.writeProperties(productId,deviceId, props);
    }

    //设置设备属性
    @SneakyThrows
    public void writeProperties(String productId,String deviceId,
                                                     Map<String, Object> properties) {
        log.info("deviceId:{},properties:{}",deviceId,properties);
        try {
            JSONObject message = new JSONObject();
            message.put("deviceId",deviceId);
            message.put("properties",JSONObject.toJSON(properties));
            String topic = "/" + productId + "/" + deviceId + "/properties/report";
            log.info("writeProperties-topic:{},message:{}",topic,message.toString());
            String redisKey = "mqtt:"+deviceId;
            redisUtil.set(redisKey,deviceId);
            mqttConnect.pub(topic, message.toString());
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

}
