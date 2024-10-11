package com.rk.utils;
import java.util.HashMap;

public class R extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;

    public R() {
        this.put((String)"code", 1000);
        this.put((String)"message", "操作成功");
        this.put((String)"data", (Object)null);
    }

    public static R error() {
        return error(500, "操作失败");
    }

    public static R error(int code, String msg) {
        R r = new R();
        r.put((String)"code", code);
        r.put((String)"message", msg);
        return r;
    }

    public static R error(int code, String msg, Object data) {
        R r = new R();
        r.put((String)"code", code);
        r.put((String)"message", msg);
        r.put("data", data);
        return r;
    }

    public static R ok() {
        return new R();
    }

    public static R ok(Object data) {
        R r = new R();
        r.put("data", data);
        return r;
    }

    public static R ok(String msg, Object data) {
        R r = new R();
        r.put((String)"message", msg);
        r.put("data", data);
        return r;
    }

    public R put(String key, Object value) {
        super.put(key, value);
        return this;
    }
}
