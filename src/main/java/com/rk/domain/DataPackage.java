package com.rk.domain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataPackage {
    private static final Logger logger = LoggerFactory.getLogger(DataPackage.class);
    private String QN;
    private String CN;
    private String AC;
    private String MN;
    private Map<String, String> data;
    private List<Map<String, String>> response;

    public DataPackage() {
    }

    public String getQN() {
        return this.QN;
    }

    public void setQN(String qN) {
        this.QN = qN;
    }

    public String getCN() {
        return this.CN;
    }

    public void setCN(String cN) {
        this.CN = cN;
    }

    public String getAC() {
        return this.AC;
    }

    public void setAC(String aC) {
        this.AC = aC;
    }

    public String getMN() {
        return this.MN;
    }

    public void setMN(String mN) {
        this.MN = mN;
    }

    public Map<String, String> getData() {
        return this.data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    public List<Map<String, String>> getResponse() {
        return this.response;
    }

    public void setResponse(List<Map<String, String>> response) {
        this.response = response;
    }

    public String toStr() {
        Map<String, String> map = new HashMap();
        map.put("AC", this.getAC());
        map.put("CN", this.getCN());
        map.put("Data", convertToStr(this.getData(), ",", ":"));
        map.put("MN", this.getMN());
        map.put("QN", this.getQN());
        return "##" + convertToStr(map, ";", "=") + "_$\r\n";
    }

    public String toAck() {
        DataPackage dp = new DataPackage();
        dp.setAC("A");
        dp.setQN(this.getQN());
        dp.setCN(this.getCN());
        dp.setMN(this.getMN());
        Map<String, String> data = new HashMap();
        data.put("res", "ok");
        dp.setData(data);
        return dp.toStr();
    }

    public static DataPackage from(String str) {
        if (str.startsWith("##") && str.endsWith("_$")) {
            try {
                str = str.replace("_$", "");
                String data = str.substring(2).toLowerCase();
                if (data.indexOf("##") > 0) {
                    String[] datas = data.split("##");
                    data = datas[0];
                }

                Map<String, String> mp = convertToMap(data, ";", "=");
                DataPackage dp = new DataPackage();
                if (mp.containsKey("ac")) {
                    dp.setAC(((String)mp.get("ac")).toUpperCase());
                }

                if (mp.containsKey("cn")) {
                    dp.setCN((String)mp.get("cn"));
                }

                if (mp.containsKey("mn")) {
                    dp.setMN((String)mp.get("mn"));
                }

                if (mp.containsKey("qn")) {
                    dp.setQN((String)mp.get("qn"));
                }

                if (mp.containsKey("data")) {
                    Map<String, String> dataMap = convertToMap((String)mp.get("data"), ",", ":");
                    dp.setData(dataMap);
                }

                return dp;
            } catch (Exception var5) {
                logger.info("接收数据解析错误：" + str, var5);
                return null;
            }
        } else {
            return null;
        }
    }

    public static String convertToStr(Map<String, String> map, String itemSeparator, String kvSeparator) {
        List<String> list = new ArrayList();
        Iterator var4 = map.keySet().iterator();

        while(var4.hasNext()) {
            String key = (String)var4.next();
            list.add(key + kvSeparator + (String)map.get(key));
        }

        return String.join(itemSeparator, list);
    }

    public static Map<String, String> convertToMap(String data, String itemSeparator, String kvSeparator) {
        String[] itemSeparatorStr = data.split(itemSeparator);
        Map<String, String> dataMap = new HashMap();
        String[] var5 = itemSeparatorStr;
        int var6 = itemSeparatorStr.length;

        for(int var7 = 0; var7 < var6; ++var7) {
            String key = var5[var7];
            String[] kvSeparatorStr = key.split(kvSeparator, 2);
            dataMap.put(kvSeparatorStr[0], kvSeparatorStr[1]);
        }

        return dataMap;
    }

    public static List<Map<String, String>> convertToList(Map<String, String> map) {
        Set<String> keySet = map.keySet();
        List<Map<String, String>> toList = new ArrayList();
        Iterator var3 = keySet.iterator();

        while(var3.hasNext()) {
            String key = (String)var3.next();
            Map<String, String> dataMap = new HashMap();
            dataMap.put("name", key);
            dataMap.put("value", map.get(key));
            toList.add(dataMap);
        }

        return toList;
    }
}
