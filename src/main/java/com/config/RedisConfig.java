package com.config;

import org.apache.commons.lang.mutable.MutableObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, MutableObject> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, MutableObject> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // 使用 JdkSerializationRedisSerializer
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }
}