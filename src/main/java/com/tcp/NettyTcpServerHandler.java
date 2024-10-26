package com.tcp;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.config.RedisUtil;
import com.rk.domain.DeriveMetadataValueVo;
import com.rk.domain.DeviceInstancesTcpTemplateEntity;
import com.rk.domain.DeviceModel;
import com.rk.domain.ProductProperties;
import com.rk.service.DeviceInstanceService;
import com.rk.service.DeviceTcpInstanceService;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class NettyTcpServerHandler extends ChannelInboundHandlerAdapter {

    private final RedisUtil redisUtil;

    private final DeviceTcpInstanceService deviceTcpInstanceService;

    private final DeviceInstanceService deviceInstanceService;

    private final String tcpHeartbeat = "383633313231303737393134313930";

    // 构造函数注入RedisUtil
    public NettyTcpServerHandler(RedisUtil redisUtil, DeviceInstanceService deviceInstanceService, DeviceTcpInstanceService deviceTcpInstanceService) {
        this.redisUtil = redisUtil;
        this.deviceTcpInstanceService = deviceTcpInstanceService;
        this.deviceInstanceService = deviceInstanceService;
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

   /**
     * @param ctx
     * @author xiongchuan on 2019/4/28 16:10
     * @DESCRIPTION: 有客户端发消息会触发此函数
     * @return: void
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            String channelId = ctx.channel().id() + "";
            String hex=  msg.toString().trim();
            log.info("加载客户端报文......");
            log.info("【" + channelId + "】" + " :" + hex);
            if(tcpHeartbeat.equals(hex)){
                return;
            }
            log.info("接收原始数据1:{}: " + hex);
            try {
                String modelId = "";
                String sendHex = "";
                String deviceAddress = "";
                String result = "";
                String deviceId = "";
                if(hex.length() > 32) {
                    modelId = hex.substring(0, 30);
                    result = hex.substring(modelId.length());
                    deviceAddress = hex.substring(30, 32);
                    DeviceModel queryDeviceModel = deviceInstanceService.selectChannelByDeviceId(modelId, null);
                    System.out.println("perStr:"+modelId+";deviceAddress:"+deviceAddress+";result="+result);
                    if(!Objects.isNull(queryDeviceModel)){
                        queryDeviceModel.setChannel(channelId);
                        deviceInstanceService.updateDeviceModelByDeviceId(channelId,modelId,deviceAddress);
                    }else {
                        DeviceModel deviceModel = new DeviceModel();
                        deviceModel.setModelId(modelId);
                        deviceModel.setChannel(channelId);
                        deviceModel.setDeviceAddress(deviceAddress);
                        deviceInstanceService.insertDeviceModel(deviceModel);
                    }
                }
                deviceId = deviceInstanceService.selectDeviceIdByAddress(deviceAddress);
                System.out.println("deviceId:"+deviceId+";result="+result);
                hexBuild(deviceId,result);
                /*if(hex.length() > 48){
                    modelId = hex.substring(0, 30);
                    sendHex = hex.substring(30, 46);
                    int index = hex.indexOf(modelId);
                    result = hex.substring(index + modelId.length());
                    deviceAddress = hex.substring(46, 48);
                    System.out.println("perStr:"+modelId+"deviceAddress:"+deviceAddress+";sendHex:"+sendHex+";result="+result);
                }else if(hex.length() > 32){
                    //不带发送指令
                    modelId = hex.substring(0, 30);
                    result = hex.substring(modelId.length());
                    deviceAddress = hex.substring(30, 32);
                    System.out.println("perStr:"+modelId+";deviceAddress:"+deviceAddress+";result="+result);
                }
                if(StringUtils.isNotBlank(modelId) && StringUtils.isNotBlank(deviceAddress)){
                    //根据4g模块和设备地址查询通道信息
                    DeviceModel queryDeviceModel = deviceInstanceService.selectChannelByDeviceId(modelId, deviceAddress);
                    if(!Objects.isNull(queryDeviceModel)){
                        queryDeviceModel.setChannel(channelId);
                        deviceInstanceService.updateDeviceModelByDeviceId(channelId,modelId,deviceAddress);
                    }else {
                        DeviceModel deviceModel = new DeviceModel();
                        deviceModel.setModelId(modelId);
                        deviceModel.setChannel(channelId);
                        deviceModel.setDeviceAddress(deviceAddress);
                        deviceInstanceService.insertDeviceModel(deviceModel);
                    }
                    //不带发送指令,地址码查询
                    deviceId = deviceInstanceService.selectTcpTempBySendHex(sendHex);
                    if(StringUtils.isBlank(deviceId)){
                        //根据地址码查询设备id
                        deviceId = deviceInstanceService.selectDeviceIdByAddress(deviceAddress);
                    }
                    //根据sendHex查询数据库是否存在
                    if(StringUtils.isNotBlank(deviceId)){
                        hexBuild(deviceId,result);
                    }
                }*/
            } catch (Exception e) {
                e.printStackTrace();
            }
            //响应客户端
            ctx.write(channelId);
            return;
        }catch (Exception e){
            e.printStackTrace();
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
            //将客户端的信息直接返回写入ctx
            ByteBuf bufAck = ctx.alloc().buffer();
            byte[] payload = hexStringToByteArray(msg);
//            byte[] payload = msg.getBytes("UTF-8");
//            byte[] payload = msg.getBytes(Charset.forName("GBK"));
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

    public void hexBuild(String deviceId,String convertedHexString){
        Integer deviceType = 1;
        Integer startFunction = 4;
        log.info("sendHex:{}",convertedHexString);
        int paramNum = 0;
        List<DeviceInstancesTcpTemplateEntity> tcpTemplateByDeviceIds = deviceTcpInstanceService.findTcpTemplateByDeviceId(deviceId,paramNum);
        if(!CollectionUtils.isEmpty(tcpTemplateByDeviceIds)){
            DeviceInstancesTcpTemplateEntity tcpTemplateByDeviceId = tcpTemplateByDeviceIds.get(0);
            if(!Objects.isNull(tcpTemplateByDeviceId)){
                deviceType = tcpTemplateByDeviceId.getDeviceType();
                String isPrefix = tcpTemplateByDeviceId.getIsPrefix();
                if("0".equals(isPrefix)){
                    //不带发送指令,直接解析不需要截取
                    deviceId = tcpTemplateByDeviceId.getDeviceId();
                    deviceType = tcpTemplateByDeviceId.getDeviceType();
                }else {
                    String sendHex = convertedHexString.substring(0,16);
                    DeviceInstancesTcpTemplateEntity deviceInstancesTcpTemplateEntity = deviceTcpInstanceService.findDeviceInstanceTcpTemplateByCrc(sendHex);
                    log.info("deviceInstancesTcpTemplateEntity:{}", JSONObject.toJSONString(deviceInstancesTcpTemplateEntity));
                    if(!Objects.isNull(deviceInstancesTcpTemplateEntity)){
                        //tcp协议解析
                        deviceId = deviceInstancesTcpTemplateEntity.getDeviceId();
                        deviceType = deviceInstancesTcpTemplateEntity.getDeviceType();
                        // 查找 bb 在 aa 中的起始位置
                        int index = convertedHexString.indexOf(sendHex);
                        // 生成最新的
                        convertedHexString = convertedHexString.substring(index + sendHex.length());
                        log.info("tcp协议convertedHexString:{},deviceId:{},deviceType:{}",convertedHexString,deviceId,deviceType);
                    }
                }
            }
        }
        if (1 == deviceType) {
            //属性设备
            String metadata = deviceInstanceService.selectMataDataById(deviceId);
            if (StringUtils.isNotBlank(metadata)) {
                List<ProductProperties> propertiesList = new ArrayList<>();
                List<Integer> metricsList = new ArrayList<>();
                JSONObject metadataJson = JSONObject.parseObject(metadata);
                JSONArray properties = metadataJson.getJSONArray("properties");
                for (int i = 0; i < properties.size(); i++) {
                    JSONObject property = properties.getJSONObject(i);
                    String value = null;
                    if (property.containsKey("expands") && property.getJSONObject("expands").containsKey("metrics")) {
                        JSONArray metrics = property.getJSONObject("expands").getJSONArray("metrics");
                        if (metrics.size() > 0) {
                            value = metrics.getJSONObject(0).getString("value");
                            if(StringUtils.isNotBlank(value)){
                                //存在metrics,标记
                                metricsList.add(i);
                            }
                        }
                    }
                }
                propertiesList = JSONArray.parseArray(metadataJson.getString("properties"), ProductProperties.class);
                Integer substring = Integer.valueOf(convertedHexString.substring(5, 6)); // 提取单个字符
                paramNum = substring / 2;
                if(paramNum != 0){
                    List<DeviceInstancesTcpTemplateEntity> tcpTemplateByDeviceIdOne = deviceTcpInstanceService.findTcpTemplateByDeviceId(deviceId,paramNum);
                    if(!CollectionUtils.isEmpty(tcpTemplateByDeviceIdOne) && tcpTemplateByDeviceIds.size() ==2){
                        Integer num = tcpTemplateByDeviceIdOne.get(0).getNum();
                        if(paramNum == 3){
                            propertiesList = getLastThree(propertiesList,paramNum);
                            startFunction = paramNum;
                        }else if(paramNum ==4){
                            propertiesList = getFirstThree(propertiesList,paramNum);
                            startFunction = paramNum;
                        }
                    }
                }
                List<String> hexList = HexUtils.getHexList(convertedHexString, startFunction,metricsList);
                log.info("hexList:{}", JSONObject.toJSONString(hexList));
                if (!CollectionUtils.isEmpty(hexList) && !CollectionUtils.isEmpty(propertiesList)) {
                    List<DeriveMetadataValueVo> deriveMetadataValueVos = new ArrayList<>();
                    for (int i = 0; i < hexList.size(); i++) { // Adjust t
                        ProductProperties productProperties = propertiesList.get(i);
                        DeriveMetadataValueVo deriveMetadataValueVo = new DeriveMetadataValueVo();
                        deriveMetadataValueVo.setType(productProperties.getId());
                        deriveMetadataValueVo.setValue(hexList.get(i));
                        deriveMetadataValueVo.setUpdateTime(new Date());
                        deriveMetadataValueVos.add(deriveMetadataValueVo);
                    }
                    String deriveMetadataValue = JSONArray.toJSONString(deriveMetadataValueVos);
                    log.info(JSONObject.toJSONString(deriveMetadataValue));
                    String queryMataDataValue = deviceInstanceService.selectMataDataValueById(deviceId);
                    List<DeriveMetadataValueVo> queryData = new ArrayList<>();
                    if(StringUtils.isNotBlank(queryMataDataValue)) {
                        queryData = JSONArray.parseArray(queryMataDataValue, DeriveMetadataValueVo.class);
                        deriveMetadataValueVos.addAll(queryData);
                    }
                        // 创建一个 Map 来存储最新的 SensorData
                        Map<String, DeriveMetadataValueVo> latestDataMap = new HashMap<>();
                        // 遍历列表并根据 type 和 updateTime 更新最新值
                        for (DeriveMetadataValueVo data : deriveMetadataValueVos) {
                            // 将 long 类型的时间戳转换为 Date
                            Date updateTime = data.getUpdateTime();
                            data.setUpdateTime(updateTime);
                            // 如果该 type 不在 Map 中，或者 updateTime 更新，则替换
                            if (!latestDataMap.containsKey(data.getType()) ||
                                    latestDataMap.get(data.getType()).getUpdateTime().compareTo(data.getUpdateTime()) < 0) {
                                latestDataMap.put(data.getType(), data);
                            }
                        }
                        // 获取最终结果列表
                        List<DeriveMetadataValueVo> resultList = new ArrayList<>(latestDataMap.values());
                        //update 数据库
                        deviceInstanceService.updateDeriveMetadataValueById(JSONObject.toJSONString(resultList),deviceId);
                    }
                }
            }
    }

    public static List<ProductProperties> getLastThree(List<ProductProperties> list,Integer paramNum) {
        int size = list.size();
        if (size <= paramNum) {
            return new ArrayList<>(list); // 如果列表少于等于3个元素，返回全部
        } else {
            return new ArrayList<>(list.subList(size - paramNum, size)); // 返回最后三个元素
        }
    }

    public static List<ProductProperties> getFirstThree(List<ProductProperties> list,Integer size) {
        // 确保返回不超过原列表大小的元素
        size = Math.min(list.size(), size);
        return new ArrayList<>(list.subList(0, size)); // 返回前 3 个元素，或原列表的所有元素（如果不足3）
    }
}
