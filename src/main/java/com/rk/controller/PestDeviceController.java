package com.rk.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.rk.domain.DeviceDO;
import com.rk.utils.CacheManager;
import com.rk.utils.R;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@CrossOrigin
@Controller
@RequestMapping({"/pest", ""})
/**
 * 山东仁科虫情设备
 */
public class PestDeviceController {

    @RequestMapping(
            value = {"/demo"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public R demo(@RequestBody Map<String, Object> params) {
        System.err.println(JSONUtil.toJsonStr(params));
        return R.ok();
    }

    @RequestMapping(
            value = {"/updateWormAutoMode"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public R updateWormAutoMode(@RequestBody Map<String, String> params) {
        String deviceAddr = (String)params.get("deviceAddr");
        if (StrUtil.isEmpty(deviceAddr)) {
            return R.error(1002, "参数不能为空");
        } else {
            DeviceDO device = CacheManager.getInstance().getDeviceDO(deviceAddr);
            if (device == null) {
                return R.error(1001, "设备不存在");
            } else if (device.getCtx() == null) {
                return R.error(1001, "设备不存在");
            } else {
                params.remove("deviceAddr");
                Set<String> keySet = params.keySet();
                Map<String, String> paramsMap = new HashMap();
                Iterator var6 = keySet.iterator();

                while(var6.hasNext()) {
                    String key = (String)var6.next();
                    paramsMap.put(key, ((String)params.get(key)).replace(":", "-"));
                }

                boolean boo = device.sendData(paramsMap, "Ctrl");
                return boo ? R.ok() : R.error(1001, "命令下发失败");
            }
        }
    }

    @RequestMapping(
            value = {"/getWormDeviceParams"},
            method = {RequestMethod.GET}
    )
    @ResponseBody
    public R getWormDeviceParams(String deviceAddr) {
        if (StrUtil.isEmpty(deviceAddr)) {
            return R.error(1002, "参数不能为空");
        } else {
            DeviceDO device = CacheManager.getInstance().getDeviceDO(deviceAddr);
            if (device == null) {
                return R.error(1001, "设备不存在");
            } else if (device.getCtx() == null) {
                return R.error(1001, "设备不存在");
            } else {
                Map<String, String> dictIdData = new HashMap();
                String[] key = CacheManager.getInstance().paramsData;
                String[] var5 = key;
                int var6 = key.length;

                for(int var7 = 0; var7 < var6; ++var7) {
                    String str = var5[var7];
                    dictIdData.put(str.toLowerCase(), "1");
                }

                boolean boo = device.sendData(dictIdData, "receive");
                return boo ? R.ok() : R.error(1001, "获取设备信息失败！");
            }
        }
    }

    @RequestMapping(
            value = {"/updateWormDeviceParams", "/setWormOperation"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public R updateWormDeviceParams(@RequestBody Map<String, Object> params) {
        String deviceAddr = params.get("deviceAddr").toString();
        if (params.get("dicts") != null && deviceAddr != null) {
            DeviceDO device = CacheManager.getInstance().getDeviceDO(deviceAddr);
            if (device == null) {
                return R.error(1001, "设备不存在");
            } else if (device.getCtx() == null) {
                return R.error(1001, "设备不存在");
            } else {
                JSONArray jsonArray = JSONUtil.parseArray(JSONUtil.toJsonStr(params.get("dicts")));
                List<Map> toList = JSONUtil.toList(jsonArray, Map.class);
                Map<String, String> paramsMap = new HashMap();
                Iterator var7 = toList.iterator();

                while(var7.hasNext()) {
                    Map<String, String> str = (Map)var7.next();
                    paramsMap.put(str.get("name"), ((String)str.get("value")).replace(":", "-"));
                }

                boolean boo = device.sendData(paramsMap, "Ctrl");
                return boo ? R.ok() : R.error(1001, "命令下发失败");
            }
        } else {
            return R.error(1002, "参数不能为空");
        }
    }

    @RequestMapping(
            value = {"/wormTransData"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public R wormTransData(@RequestBody Map<String, String> params) {
        String deviceAddr = (String)params.get("deviceAddr");
        String hexData = (String)params.get("hexData");
        String channel = (String)params.get("channel");
        if (!StrUtil.isEmpty(channel) && !StrUtil.isEmpty(deviceAddr) && !StrUtil.isEmpty(hexData)) {
            DeviceDO device = CacheManager.getInstance().getDeviceDO(deviceAddr);
            if (device == null) {
                return R.error(1001, "设备不存在");
            } else if (device.getCtx() == null) {
                return R.error(1001, "设备不存在");
            } else {
                Map<String, String> paramsData = new HashMap();
                paramsData.put("hexData", hexData.replace(":", "-"));
                paramsData.put("channel", channel);
                boolean boo = device.sendData(paramsData, "trans");
                return boo ? R.ok() : R.error(1001, "数据透传失败");
            }
        } else {
            return R.error(1002, "参数不能为空");
        }
    }
}
