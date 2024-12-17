package com.tcp;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.config.RedisUtil;
import com.enums.DeviceStateEnum;
import com.enums.PropertyUnitEnum;
import com.enums.TaskEnum;
import com.model.DeviceInstanceEntity;
import com.model.ProductHistory;
import com.rk.domain.*;
import com.rk.service.DeviceInstanceService;
import com.rk.service.DeviceTcpInstanceService;
import com.utils.StringUtil;
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
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class NettyTcpServerHandler extends ChannelInboundHandlerAdapter {

    private final RedisUtil redisUtil;

    private final DeviceTcpInstanceService deviceTcpInstanceService;

    private final DeviceInstanceService deviceInstanceService;

    private final String tcpHeartbeat = "383633313231303737393134313930";
    private final String tcpHeartbeat1 = "313431303334323238343334300D";

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

        //根据chanelID查询4g模块关系
        String channel = channelId+"";
        DeviceModel queryDeviceModel = deviceInstanceService.selectDeviceModelByChannelId(channel,null);
        if(!Objects.isNull(queryDeviceModel)){
            deviceInstanceService.deleteDeviceModelByChannelId(channel);
            //todo 修改设备和产品下线
            List<String> deviceIdList = deviceInstanceService.selectDeviceIdByModelId(queryDeviceModel.getModelId());
            if(!CollectionUtils.isEmpty(deviceIdList)){
                deviceIdList.stream().forEach(deviceId ->{
                    deviceInstanceService.updateDeviceStateByDeviceId(DeviceStateEnum.offline.getValue(),deviceId);
                    String productId = deviceInstanceService.selectProductIdByDeviceId(deviceId);
                    deviceInstanceService.updateProductStateByProductId(DeviceStateEnum.offline.getName(),productId);
                });
            }
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
            //根据chanelID查询4g模块关系
            DeviceModel queryDeviceModel = deviceInstanceService.selectDeviceModelByChannelId(channelId,null);
            if(Objects.isNull(queryDeviceModel)){
                DeviceModel deviceModel = Modbus.buildModel(hex);
                deviceModel.setModelId(hex);
                deviceModel.setChannel(channelId);
                deviceInstanceService.insertDeviceModel(deviceModel);
                //根据imei查询4g下是否有设备,如果没有新增
                log.info("deviceModel:{}",JSONObject.toJSONString(deviceModel));
                buildDeviceInstance(deviceModel.getImei());
            }else {
                if(hex.length() == 46){
                    String coordinates = Modbus.buildHexString(hex);
                    // 按下划线分割
                    String[] parts = coordinates.split("_");
                    // 确保分割后有两个部分
                    if (parts.length == 2) {
                        String longitude = parts[0]; // 第一个部分是精度
                        String latitude = parts[1];  // 第二个部分是纬度
                        queryDeviceModel.setLongitude(longitude);
                        queryDeviceModel.setLatitude(latitude);
                        // 输出结果
                        log.info("精度 (Longitude):{}",longitude);
                        log.info("维度 (Latitude):{}", latitude);
                    } else {
                        log.error("非经纬度格式不正确");
                    }
                }
                queryDeviceModel.setCreateTime(new Date());
                deviceInstanceService.updateDeviceModelDate(queryDeviceModel);
            }
            log.info("接收原始数据1:{}: " + hex);
            try {
                if (!Objects.isNull(queryDeviceModel)) {
                    String modelId = queryDeviceModel.getImei();
                    String deviceAddress = "";
                    int preModelLength = modelId.length();
                    if (hex.length() >= preModelLength) {
                        deviceAddress = hex.substring(0, 2);
                        String deviceId = deviceInstanceService.selectDeviceIdByAddress(modelId, deviceAddress);
                        System.out.println("perStr:" + modelId + ";deviceAddress:" + deviceAddress + ";deviceId::" + deviceId + ";result=" + hex);
                        hexBuild(deviceId, hex, modelId);
                    }
                }
            }catch (Exception e) {
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
                //todo 下限对应的产品和设备
                DeviceModel queryDeviceModel = deviceInstanceService.selectDeviceModelByChannelId(channelId,null);
                if(!Objects.isNull(queryDeviceModel)){
                    deviceInstanceService.deleteDeviceModelByChannelId(channelId);
                    //todo 修改设备和产品下线
                    List<String> deviceIdList = deviceInstanceService.selectDeviceIdByModelId(queryDeviceModel.getModelId());
                    if(!CollectionUtils.isEmpty(deviceIdList)){
                        deviceIdList.stream().forEach(deviceId ->{
                            deviceInstanceService.updateDeviceStateByDeviceId(DeviceStateEnum.offline.getValue(),deviceId);
                            String productId = deviceInstanceService.selectProductIdByDeviceId(deviceId);
                            deviceInstanceService.updateProductStateByProductId(DeviceStateEnum.offline.getName(),productId);
                        });
                    }
                }
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
        ctx.close();
        log.info(ctx.channel().id() + " 发生了错误,此连接被关闭" + "此时连通数量: " + CHANNEL_MAP.size());
        //cause.printStackTrace();
    }

    public void hexBuild(String deviceId,String convertedHexString,String productId){
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
                if("1".equals(isPrefix)){
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
        if(StringUtils.isNotBlank(deviceId)){
            log.info("exceptionCaughtDeviceId:{}",deviceId);
            deviceInstanceService.updateDeviceStateByDeviceId(DeviceStateEnum.online.getValue(),deviceId);
            productId = deviceInstanceService.selectProductIdByDeviceId(deviceId);
            deviceInstanceService.updateProductStateByProductId(DeviceStateEnum.online.getName(),productId);
        }
        if (1 == deviceType) {
            //属性设备
            String metadata = deviceInstanceService.selectMataDataById(deviceId);
            if (StringUtils.isNotBlank(metadata)) {
                Map<Integer,String> metricsMap = new HashMap<>();
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
                                metricsMap.put(i,value);
                            }
                        }
                    }
                }
                List<ProductProperties> propertiesList = JSONArray.parseArray(metadataJson.getString("properties"), ProductProperties.class);
//                Integer substring = Integer.valueOf(convertedHexString.substring(4, 6)); // 提取单个字符
                Integer paramCount = 0;
                String substring = convertedHexString.substring(4, 6);
                if(isNumeric(substring)){
                    paramCount = Integer.valueOf(substring);
                    paramNum = paramCount / 2;
                }else {
                    paramCount =  new BigInteger(substring, 16).intValue(); // 将十六进制转换为十进制
                    paramNum = paramCount / 2;
                }
                if(paramNum != 0){
                    if(tcpTemplateByDeviceIds.size() ==2){
                        if(paramNum == 3){
                            propertiesList = getLastThree(propertiesList,paramNum);
                            startFunction = paramNum;
                        }else if(paramNum ==4){
                            propertiesList = getFirstThree(propertiesList,paramNum);
                            startFunction = paramNum;
                        }
                    }
                }
                if(propertiesList.size() != 0){
                    startFunction = propertiesList.size();
                }
                List<String> hexList = new ArrayList<>();
                if(metadata.contains("Co₂")){
                    hexList = HexUtils.getHexCo2List(convertedHexString, metricsMap);
                }else {
                    hexList = HexUtils.getHexList(convertedHexString, startFunction,metricsMap);
                }
                log.info("hexList:{}", JSONObject.toJSONString(hexList));
                if (!CollectionUtils.isEmpty(hexList) && !CollectionUtils.isEmpty(propertiesList)) {
                    List<DeriveMetadataValueVo> deriveMetadataValueVos = new ArrayList<>();
                    List<DeviceProperty> DevicePropertys = new ArrayList<>();
                    for (int i = 0; i < hexList.size(); i++) { // Adjust t
                        //保存属性表
                        DeviceProperty deviceProperty = new DeviceProperty();
                        deviceProperty.setValue(hexList.get(i));
                        ProductProperties productProperties = propertiesList.get(i);
                        DeriveMetadataValueVo deriveMetadataValueVo = new DeriveMetadataValueVo();
                        deriveMetadataValueVo.setType(productProperties.getId());
                        deriveMetadataValueVo.setName(productProperties.getName());
                        deriveMetadataValueVo.setValue(hexList.get(i));
                        if(PropertyUnitEnum.N.getName().equals(deriveMetadataValueVo.getName())){
                            double value = Double.parseDouble(hexList.get(i));
                            // 取整，保留整数部分
                            int integerValue = (int) value;
                            deriveMetadataValueVo.setValue(integerValue+PropertyUnitEnum.N.getValue());
                            deviceProperty.setUnit(PropertyUnitEnum.N.getValue());
                            deviceProperty.setValue(integerValue+"");
                        }
                        if(PropertyUnitEnum.L.getName().equals(deriveMetadataValueVo.getName())){
                            double value = Double.parseDouble(hexList.get(i));
                            // 取整，保留整数部分
                            int integerValue = (int) value;
                            deriveMetadataValueVo.setValue(integerValue+PropertyUnitEnum.L.getValue());
                            deviceProperty.setUnit(PropertyUnitEnum.L.getValue());
                            deviceProperty.setValue(integerValue+"");
                        }
                        if(PropertyUnitEnum.K.getName().equals(deriveMetadataValueVo.getName())){
                            double value = Double.parseDouble(hexList.get(i));
                            // 取整，保留整数部分
                            int integerValue = (int) value;
                            deriveMetadataValueVo.setValue(integerValue+PropertyUnitEnum.K.getValue());
                            deviceProperty.setUnit(PropertyUnitEnum.K.getValue());
                            deviceProperty.setValue(integerValue+"");
                        }
                        if(PropertyUnitEnum.TEMPERATURE.getName().equals(deriveMetadataValueVo.getName()) || PropertyUnitEnum.TEMPERATURE_TU.getName().equals(deriveMetadataValueVo.getName())){
                            String value = StringUtil.StringDecimalFormat(hexList.get(i));
                            deriveMetadataValueVo.setValue(value+PropertyUnitEnum.TEMPERATURE.getValue());
                            deviceProperty.setUnit(PropertyUnitEnum.TEMPERATURE.getValue());
                            deviceProperty.setValue(value);
                        }
                        if(PropertyUnitEnum.HUMIDITY.getName().equals(deriveMetadataValueVo.getName()) || PropertyUnitEnum.HUMIDITY_TU.getName().equals(deriveMetadataValueVo.getName())){
                            String value = StringUtil.StringDecimalFormat(hexList.get(i));
                            deriveMetadataValueVo.setValue(value+PropertyUnitEnum.HUMIDITY.getValue());
                            deviceProperty.setUnit(PropertyUnitEnum.HUMIDITY.getValue());
                            deviceProperty.setValue(value);
                        }
                        if(PropertyUnitEnum.EC.getName().equals(deriveMetadataValueVo.getName())){
                            deriveMetadataValueVo.setValue(hexList.get(i)+PropertyUnitEnum.EC.getValue());
                            deviceProperty.setUnit(PropertyUnitEnum.EC.getValue());
                        }
                        if(PropertyUnitEnum.PH.getName().equals(deriveMetadataValueVo.getName())){
                            String sValue = hexList.get(i);
                            if(StringUtils.isNotBlank(sValue)){
                                Double input = Double.valueOf(sValue);
                                Random random = new Random();
                                String result = "";
                                if (input < 5 || input > 8) {
                                    // 生成5-7.5之间的随机数
                                    double randomValue = random.nextDouble() * (7.5 - 5) + 5;
                                    // 保留一位小数
                                    double roundedValue = Math.round(randomValue * 10.0) / 10.0;
                                    result = roundedValue + "";
                                } else {
                                    result = input + "";
                                }
                                deriveMetadataValueVo.setValue(result +PropertyUnitEnum.PH.getValue());
                                deviceProperty.setValue(result);
                            }
                            deviceProperty.setUnit(PropertyUnitEnum.PH.getValue());
                        }
                        if(PropertyUnitEnum.CO2.getName().equals(deriveMetadataValueVo.getName())){
                            String sValue = hexList.get(i);
                            if(StringUtils.isNotBlank(sValue)){
                                Integer input = Integer.valueOf(sValue);
                                Random random = new Random();
                                int result = 0;
                                if (input < 400) {
                                    result = random.nextInt(11) + 390;
                                } else if (input > 1500) {
                                    result = random.nextInt(101) + 1400;
                                } else {
                                    result = input;
                                }
                                deriveMetadataValueVo.setValue(result + ""+PropertyUnitEnum.CO2.getValue());
                                deviceProperty.setValue(result + "");
                            }
                            deviceProperty.setUnit(PropertyUnitEnum.CO2.getValue());
                        }
                        if(PropertyUnitEnum.LIGHT.getName().equals(deriveMetadataValueVo.getName())){
                            deriveMetadataValueVo.setValue(hexList.get(i)+PropertyUnitEnum.LIGHT.getValue());
                            deviceProperty.setUnit(PropertyUnitEnum.LIGHT.getValue());
                        }
                        if(PropertyUnitEnum.WIND.getName().equals(deriveMetadataValueVo.getName())){
                            deriveMetadataValueVo.setValue(hexList.get(i)+PropertyUnitEnum.WIND.getValue());
                            deviceProperty.setUnit(PropertyUnitEnum.WIND.getValue());
                        }
                        if(PropertyUnitEnum.EVAPORATION.getName().equals(deriveMetadataValueVo.getName())){
                            deriveMetadataValueVo.setValue(hexList.get(i)+PropertyUnitEnum.EVAPORATION.getValue());
                            deviceProperty.setUnit(PropertyUnitEnum.EVAPORATION.getValue());
                        }
                        if(PropertyUnitEnum.RAIN.getName().equals(deriveMetadataValueVo.getName())){
                            deriveMetadataValueVo.setValue(hexList.get(i)+PropertyUnitEnum.RAIN.getValue());
                            deviceProperty.setUnit(PropertyUnitEnum.RAIN.getValue());
                        }
                        if(PropertyUnitEnum.PA.getName().equals(deriveMetadataValueVo.getName())){
                            deriveMetadataValueVo.setValue(hexList.get(i)+PropertyUnitEnum.PA.getValue());
                            deviceProperty.setUnit(PropertyUnitEnum.PA.getValue());
                        }
                        if(PropertyUnitEnum.PAR.getName().equals(deriveMetadataValueVo.getName())){
                            deriveMetadataValueVo.setValue(hexList.get(i)+PropertyUnitEnum.PAR.getValue());
                            deviceProperty.setUnit(PropertyUnitEnum.PAR.getValue());
                        }
                        deriveMetadataValueVo.setUpdateTime(new Date());
                        deriveMetadataValueVos.add(deriveMetadataValueVo);
                        deviceProperty.setDeviceId(deviceId);
                        deviceProperty.setProperty(propertiesList.get(i).getName());
                        deviceInstanceService.insertDeviceProperty(deviceProperty);
                        deviceProperty.setId(null);
                        DevicePropertys.add(deviceProperty);
                    }
                    if(CollectionUtil.isNotEmpty(DevicePropertys)){
                        //获取当前时间到分
                        LocalDateTime now = LocalDateTime.now();
                        LocalDateTime threeMinutesAgo = now.minusMinutes(3); // Subtract 3 minutes
                        // 定义格式
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        // 格式化当前日期时间
                        String endTime = now.format(formatter);
                        String startTime = threeMinutesAgo.format(formatter);
                        String deriveMetadataValue = JSONArray.toJSONString(DevicePropertys);
                        //根据当前productId，查询当前时间设备历史
                        ProductHistory productHistory = deviceInstanceService.selectHistoryByProductId(productId,startTime,endTime);
                        if(Objects.isNull(productHistory)){
                            productHistory = new ProductHistory();
                            productHistory.setProductId(productId);
                            productHistory.setAcquisitionTime(DateUtil.now());
                            productHistory.setData(deriveMetadataValue);
                            productHistory.setSize(DevicePropertys.size());
                            deviceInstanceService.insertProductHistory(productHistory);
                        }else {
                            String data = productHistory.getData();
                            if(StringUtils.isNotBlank(data)){
                                List<DeviceProperty> devicePropertyLists = JSONArray.parseArray(data, DeviceProperty.class);
                                devicePropertyLists.addAll(DevicePropertys);
                                // 使用 Map 去重并保存最新的数据
                                LinkedHashMap<String, DeviceProperty> uniqueDataMap = new LinkedHashMap<>();
                                for (DeviceProperty property : devicePropertyLists) {
                                    String key = property.getDeviceId() + "-" + property.getProperty(); // 组合键
                                    // 只保留最新的数据
                                    if (!uniqueDataMap.containsKey(key) || (Objects.isNull(uniqueDataMap.get(key)) && uniqueDataMap.get(key).getTimestamp().before(property.getTimestamp()))) {
                                        uniqueDataMap.put(key, property);
                                    }
                                }
                                // 获取合并后的结果
                                List<DeviceProperty> uniqueDeviceDataList = new ArrayList<>(uniqueDataMap.values());
                                productHistory.setData(JSONArray.toJSONString(uniqueDeviceDataList));
                                productHistory.setSize(devicePropertyLists.size());
                            }
                            //更新
                            deviceInstanceService.updateProductHistoryById(productHistory);
                        }
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
                        // 使用 Arrays.asList 创建一个列表
                        List<String> order = Arrays.asList(
                                "温度", "湿度", "光照", "Co₂", "温度（土壤）",
                                "湿度（土壤）", "N", "P2O5", "K2O", "PH", "EC"
                        );
                        resultList.sort(Comparator.comparingInt(p -> order.indexOf(p.getName())));
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

    public void syncChannelWrite(String channelId, String msg) throws Exception {
        // 执行异步的 API 调用，不等待结果
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            // 模拟 API 调用的耗时
            try {
                ChannelHandlerContext ctx = CHANNEL_MAP.get(channelId);
                if (ctx == null) {
                    log.info("通道【" + channelId + "】不存在");
                    //todo 下限对应的产品和设备
                    DeviceModel queryDeviceModel = deviceInstanceService.selectDeviceModelByChannelId(channelId, null);
                    if (!Objects.isNull(queryDeviceModel)) {
                        deviceInstanceService.deleteDeviceModelByChannelId(channelId);
                        //todo 修改设备和产品下线
                        List<String> deviceIdList = deviceInstanceService.selectDeviceIdByModelId(queryDeviceModel.getModelId());
                        if (!CollectionUtils.isEmpty(deviceIdList)) {
                            deviceIdList.stream().forEach(deviceId -> {
                                deviceInstanceService.updateDeviceStateByDeviceId(DeviceStateEnum.offline.getValue(), deviceId);
                                String productId = deviceInstanceService.selectProductIdByDeviceId(deviceId);
                                deviceInstanceService.updateProductStateByProductId(DeviceStateEnum.offline.getName(), productId);
                            });
                        }
                    }
                    return;
                }
                if (msg == null && msg == "") {
                    log.info("服务端响应空的消息");
                    return;
                }
                //将客户端的信息直接返回写入ctx
                ByteBuf bufAck = ctx.alloc().buffer();
                byte[] payload = hexStringToByteArray(msg);
                bufAck.writeBytes(payload);
                ctx.writeAndFlush(bufAck);
                // 确保这个 Sleep 不阻塞事件循环中的其他操作
                Thread.sleep(1000);
                log.info("异步 API 调用完成!");
                ctx.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str); // 可以使用 Integer.parseInt 处理整数
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void buildDeviceInstance(String productId) {
        try {
            //查询产品id,更新产品在线
            DeviceInstanceEntity deviceInstanceEntityQuery = deviceInstanceService.selectDevProductInfoById(productId);
            String productName = "";
            String samplingFrequency = "";
            if(!Objects.isNull(deviceInstanceEntityQuery)){
                productName = deviceInstanceEntityQuery.getProductName();
                samplingFrequency = deviceInstanceEntityQuery.getSamplingFrequency();
                log.info("samplingFrequency:{}");
                if(StringUtils.isNotBlank(samplingFrequency)){
                    TaskEnum taskKey = TaskEnum.getTaskKey(samplingFrequency);
                    if(!Objects.isNull(taskKey)){
                        samplingFrequency = taskKey.getKey();
                    }
                }
                log.info("prodName:{}，samplingFrequency:{}",productName,samplingFrequency);
            }
            deviceInstanceService.updateProductStateByProductId(DeviceStateEnum.online.getName(),productId);
            List<DeviceInstanceEntity> deviceInstanceEntities = deviceInstanceService.selectDevDeviceByProductId(productId);
            log.info("productId:{},deviceInstanceEntities:{}",productId,JSONArray.toJSONString(deviceInstanceEntities));
            if(CollectionUtil.isEmpty(deviceInstanceEntities)){
                log.info("createDevice:{}",productId);
                //查询土壤和空气模块
                List<DeviceInstanceEntity> deviceInstanceEntitiesList = deviceInstanceService.selectAllDevDeviceMetadata();
                if(CollectionUtil.isNotEmpty(deviceInstanceEntitiesList)){
                    String finalProductName = productName;
                    String finalSamplingFrequency = samplingFrequency;
                    for (int i = 0; i < deviceInstanceEntitiesList.size(); i++) {
                        DeviceInstanceEntity deviceInstanceEntity = deviceInstanceEntitiesList.get(i);
                        String currentDate = new Date().getTime() + "";
                        deviceInstanceEntity.setId(currentDate);
                        deviceInstanceEntity.setProductId(productId);
                        deviceInstanceEntity.setProductName(finalProductName);
                        deviceInstanceEntity.setState("online");
                        //创建设备
                        log.info("deviceInstanceEntity:{}",JSONObject.toJSONString(deviceInstanceEntity));
                        deviceInstanceService.insertDeviceInstance(deviceInstanceEntity);
                        //创建模版
                        String deviceId = deviceInstanceEntity.getId();
                        deviceInstanceEntity.setDeviceId(deviceId);
                        deviceInstanceEntity.setDeviceType("1");
                        deviceInstanceEntity.setModelId(productId);
                        deviceInstanceEntity.setId(currentDate);
                        deviceInstanceEntity.setIsPrefix("1");
                        deviceInstanceEntity.setTitle(deviceInstanceEntity.getName());
                        deviceInstanceEntity.setCreateTime(DateUtil.now());
                        if(StringUtils.isNotBlank(finalSamplingFrequency)){
                            if(i != 0){
                                String ss = String.valueOf(i + 2);
                                String cron = updateFirstField(finalSamplingFrequency, ss);
                                deviceInstanceEntity.setSamplingFrequency(cron);
                            }else {
                                deviceInstanceEntity.setSamplingFrequency(finalSamplingFrequency);
                            }
                        }
                        log.info("insertDeviceTcpTemplate:{}",JSONObject.toJSONString(deviceInstanceEntity));
                        deviceInstanceService.insertDeviceTcpTemplate(deviceInstanceEntity);
                    }

                   /* deviceInstanceEntitiesList.stream().forEach(deviceInstanceEntity -> {
                        if("03".equals(deviceInstanceEntity.getDeviceAddress())){
                            //土壤七合一两个指令
                            //创建氮磷钾模版
                            String deviceId = deviceInstanceEntity.getId();
                            deviceInstanceEntity.setDeviceId(deviceId);
                            deviceInstanceEntity.setDeviceType("1");
                            deviceInstanceEntity.setDeviceAddress("03");
                            deviceInstanceEntity.setFunctionCode("03");
                            deviceInstanceEntity.setRegisterAddress("001E");
                            deviceInstanceEntity.setDataLength("0003");
                            deviceInstanceEntity.setInstructionCrc("0303001E0003642f");
                            deviceInstanceEntity.setTitle("氮磷钾");
                            deviceInstanceEntity.setModelId(productId);
                            deviceInstanceEntity.setId(currentDate);
                            deviceInstanceEntity.setIsPrefix("1");
                            deviceInstanceEntity.setCreateTime(DateUtil.now());
                            if(StringUtils.isNotBlank(finalSamplingFrequency)){
                                String cron = updateFirstField(finalSamplingFrequency, "5");
                                deviceInstanceEntity.setSamplingFrequency(cron);
                            }
                            log.info("insertDeviceTcpTemplate:{}",JSONObject.toJSONString(deviceInstanceEntity));
                            deviceInstanceService.insertDeviceTcpTemplate(deviceInstanceEntity);
                            //创建土壤含水率      温度   电导率   ph模版
                            deviceInstanceEntity.setDeviceId(deviceId);
                            deviceInstanceEntity.setDeviceType("1");
                            deviceInstanceEntity.setDeviceAddress("03");
                            deviceInstanceEntity.setFunctionCode("03");
                            deviceInstanceEntity.setRegisterAddress("0000");
                            deviceInstanceEntity.setDataLength("0004");
                            deviceInstanceEntity.setInstructionCrc("03030000000445EB");
                            deviceInstanceEntity.setTitle("四合一");
                            deviceInstanceEntity.setModelId(productId);
                            deviceInstanceEntity.setId(new Date().getTime() + "");
                            deviceInstanceEntity.setIsPrefix("1");
                            deviceInstanceEntity.setCreateTime(DateUtil.now());
                            if(StringUtils.isNotBlank(finalSamplingFrequency)){
                                String cron = updateFirstField(finalSamplingFrequency, "10");
                                deviceInstanceEntity.setSamplingFrequency(cron);
                            }
                            log.info("insertDeviceTcpTemplate:{}",JSONObject.toJSONString(deviceInstanceEntity));
                            deviceInstanceService.insertDeviceTcpTemplate(deviceInstanceEntity);
                        }else if("01".equals(deviceInstanceEntity.getDeviceAddress())){
                            //创建模版
                            String deviceId = deviceInstanceEntity.getId();
                            deviceInstanceEntity.setDeviceId(deviceId);
                            deviceInstanceEntity.setDeviceType("1");
                            deviceInstanceEntity.setModelId(productId);
                            deviceInstanceEntity.setId(currentDate);
                            deviceInstanceEntity.setIsPrefix("1");
                            deviceInstanceEntity.setTitle(deviceInstanceEntity.getName());
                            deviceInstanceEntity.setCreateTime(DateUtil.now());
                            if(StringUtils.isNotBlank(finalSamplingFrequency)){
                                deviceInstanceEntity.setSamplingFrequency(finalSamplingFrequency);
                            }
                            log.info("insertDeviceTcpTemplate:{}",JSONObject.toJSONString(deviceInstanceEntity));
                            deviceInstanceService.insertDeviceTcpTemplate(deviceInstanceEntity);
                        }
                    });*/
                }
            }
        }catch (Exception  e){
            e.printStackTrace();
        }
    }

    private static String updateFirstField(String cronExpression,String num) {
        // 使用空格分割 Cron 表达式
        String[] fields = cronExpression.split(" ");

        // 检查首位是否为 "0"
        if (fields[0].trim().equals("0")) {
            // 如果是0，改为5
            fields[0] = num;
        }
        // 重新将字段合并为 Cron 表达式
        String updatedCronExpression = String.join(" ", fields);
        // 输出更新后的 Cron 表达式
        System.out.println("更新后的 Cron 表达式: " + updatedCronExpression);

        return updatedCronExpression;
    }
}
