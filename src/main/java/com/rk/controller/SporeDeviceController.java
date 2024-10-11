package com.rk.controller;

import com.rk.domain.DeviceDO;
import com.rk.utils.CacheManager;
import com.rk.utils.R;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@CrossOrigin
@Controller
@RequestMapping({"spore", ""})
/**
 * 山东仁科孢子设备
 */
public class SporeDeviceController {

    @RequestMapping(
            value = {"/modeSwitch"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public R modeSwitch(@RequestBody Map<String, String> params) {
        String deviceAddr = (String)params.get("deviceAddr");
        String mode = (String)params.get("mode");
        DeviceDO device = CacheManager.getInstance().getDeviceDO(deviceAddr);
        if (device == null) {
            return R.error(1001, "设备不存在");
        } else if (device.getCtx() == null) {
            return R.error(1001, "设备不存在");
        } else {
            Map<String, String> paramsData = new HashMap();
            byte var7 = -1;
            switch(mode.hashCode()) {
                case 48:
                    if (mode.equals("0")) {
                        var7 = 1;
                    }
                    break;
                case 49:
                    if (mode.equals("1")) {
                        var7 = 0;
                    }
            }

            switch(var7) {
                case 0:
                case 1:
                    paramsData.put("controlMode", mode);
                    boolean var6 = device.sendData(paramsData, "Ctrl");
                    if (var6) {
                        return R.ok();
                    }

                    return R.error(1001, "设备已离线");
                default:
                    return R.error(1001, "操作命令错误");
            }
        }
    }

    @RequestMapping(
            value = {"/issueInstructions"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public R issueInstructions(@RequestBody Map<String, String> params) {
        String deviceAddr = (String)params.get("deviceAddr");
        String module = (String)params.get("module");
        String opt = (String)params.get("opt");
        DeviceDO device = CacheManager.getInstance().getDeviceDO(deviceAddr);
        if (device == null) {
            return R.error(1001, "设备不存在");
        } else if (device.getCtx() == null) {
            return R.error(1001, "设备不存在");
        } else {
            Map<String, String> paramsData = new HashMap();
            byte var8 = -1;
            switch(opt.hashCode()) {
                case 48:
                    if (opt.equals("0")) {
                        var8 = 1;
                    }
                    break;
                case 49:
                    if (opt.equals("1")) {
                        var8 = 0;
                    }
            }

            switch(var8) {
                case 0:
                case 1:
                    paramsData.put(module, opt);
                    boolean var7 = device.sendData(paramsData, "Ctrl");
                    if (var7) {
                        return R.ok();
                    }

                    return R.error(1001, "设备已离线");
                default:
                    return R.error(1001, "操作命令错误");
            }
        }
    }

    @RequestMapping(
            value = {"/updateAutoMode"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public R updateAutoMode(@RequestBody Map<String, String> params) {
        String deviceAddr = (String)params.get("deviceAddr");
        if (deviceAddr == null) {
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
}

