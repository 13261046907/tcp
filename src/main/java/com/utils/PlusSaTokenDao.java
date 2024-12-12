package com.utils;

import cn.dev33.satoken.dao.SaTokenDao;
import com.config.RedisUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * Sa-Token持久层接口(使用框架自带RedisUtils实现 协议统一)
 *
 * @author Lion Li
 */
@Component
public class PlusSaTokenDao implements SaTokenDao {
    @Resource
    private RedisUtil redisUtil;

    /**
     * 获取Value，如无返空
     */
    @Override
    public String get(String key) {
        Object object = redisUtil.get(key);
        return object != null ? object.toString() : null;
    }

    /**
     * 写入Value，并设定存活时间 (单位: 秒)
     */
    @Override
    public void set(String key, String value, long timeout) {
        if (timeout == 0 || timeout <= NOT_VALUE_EXPIRE) {
            return;
        }
        // 判断是否为永不过期
        if (timeout == NEVER_EXPIRE) {
            redisUtil.set(key, value);
        } else {
            redisUtil.set(key, value, timeout);
        }
    }

    /**
     * 修修改指定key-value键值对 (过期时间不变)
     */
    @Override
    public void update(String key, String value) {
        long expire = getTimeout(key);
        // -2 = 无此键
        if (expire == NOT_VALUE_EXPIRE) {
            return;
        }
        this.set(key, value, expire);
    }

    /**
     * 删除Value
     */
    @Override
    public void delete(String key) {
        redisUtil.del(key);
    }

    /**
     * 获取Value的剩余存活时间 (单位: 秒)
     */
    @Override
    public long getTimeout(String key) {
        long timeout = redisUtil.getExpire(key);
        return timeout;
    }

    /**
     * 修改Value的剩余存活时间 (单位: 秒)
     */
    @Override
    public void updateTimeout(String key, long timeout) {
        // 判断是否想要设置为永久
        if (timeout == NEVER_EXPIRE) {
            long expire = getTimeout(key);
            if (expire == NEVER_EXPIRE) {
                // 如果其已经被设置为永久，则不作任何处理
            } else {
                // 如果尚未被设置为永久，那么再次set一次
                this.set(key, this.get(key), timeout);
            }
            return;
        }
        redisUtil.expire(key, timeout);
    }


    /**
     * 获取Object，如无返空
     */
    @Override
    public Object getObject(String key) {
        return redisUtil.get(key);
    }

    /**
     * 写入Object，并设定存活时间 (单位: 秒)
     */
    @Override
    public void setObject(String key, Object object, long timeout) {
        if (timeout == 0 || timeout <= NOT_VALUE_EXPIRE) {
            return;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String jsonStr = objectMapper.writeValueAsString(object);
            // 判断是否为永不过期
            if (timeout == NEVER_EXPIRE) {
                redisUtil.set(key, jsonStr);
            } else {
                redisUtil.set(key, jsonStr, timeout);
            }
        }catch (JsonProcessingException e) {
                e.printStackTrace();
        }
    }

    /**
     * 更新Object (过期时间不变)
     */
    @Override
    public void updateObject(String key, Object object) {
        long expire = getObjectTimeout(key);
        // -2 = 无此键
        if (expire == NOT_VALUE_EXPIRE) {
            return;
        }
        this.setObject(key, object, expire);
    }

    /**
     * 删除Object
     */
    @Override
    public void deleteObject(String key) {
        redisUtil.del(key);
    }

    /**
     * 获取Object的剩余存活时间 (单位: 秒)
     */
    @Override
    public long getObjectTimeout(String key) {
        long timeout = redisUtil.getExpire(key);
        return timeout;
    }

    /**
     * 修改Object的剩余存活时间 (单位: 秒)
     */
    @Override
    public void updateObjectTimeout(String key, long timeout) {
        // 判断是否想要设置为永久
        if (timeout == NEVER_EXPIRE) {
            long expire = getObjectTimeout(key);
            if (expire == NEVER_EXPIRE) {
                // 如果其已经被设置为永久，则不作任何处理
            } else {
                // 如果尚未被设置为永久，那么再次set一次
                this.setObject(key, this.getObject(key), timeout);
            }
            return;
        }
        redisUtil.expire(key, timeout);
    }


    /**
     * 搜索数据
     */
    @Override
    public List<String> searchData(String prefix, String keyword, int start, int size, boolean sortType) {
        // Collection<String> keys = redisUtil.keys(prefix + "*" + keyword + "*");
        // List<String> list = new ArrayList<>(keys);
        // return SaFoxUtil.searchList(list, start, size, sortType);
        return null;
    }
}
