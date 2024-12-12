package com.config;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.enums.ErrorEnum;
import com.exceptions.BusinessException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * description
 *
 * @author admin
 */
@Configuration
@Component
public class WebMVCConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // registry.addResourceHandler("/**")
        //         .addResourceLocations("classpath:/static/");
        // registry.addResourceHandler("swagger-ui.html")
        //         .addResourceLocations("classpath:/META-INF/resources/");
        // registry.addResourceHandler("/webjars/**")
        //         .addResourceLocations("classpath:/META-INF/resources/webjars/");
        // registry.addResourceHandler("doc.html")
        //         .addResourceLocations("classpath:/META-INF/resources/");
        // registry.addResourceHandler("/webjars/**")
        //         .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    /**
     * 跨域配置
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        // 设置访问源地址
        config.addAllowedOrigin("*");
        // 设置访问源请求头
        config.addAllowedHeader("*");
        // 设置访问源请求方法
        config.addAllowedMethod("*");
        // 有效期 1800秒
        config.setMaxAge(1800L);
        // 添加映射路径，拦截一切请求
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        // 返回新的CorsFilter
        return new CorsFilter(source);
    }

    // @Override
    // public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    //     MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
    //     ObjectMapper objectMapper = converter.getObjectMapper();
    //     // 防止JSON名称被转义
    //     // objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.KEBAB_CASE);
    //     // 将Null值改为空字符串
    //     /*objectMapper.getSerializerProvider().setNullValueSerializer(new JsonSerializer<>() {
    //         @Override
    //         public void serialize(Object value, JsonGenerator jg, SerializerProvider sp) throws IOException {
    //             jg.writeString("");
    //         }
    //     });*/
    //     objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    //     objectMapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
    //     // 对于空的对象转json的时候不抛出错误
    //     objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    //     // 禁用遇到未知属性抛出异常
    //     objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    //     objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
    //
    //     // 将Long序列成String
    //     SimpleModule simpleModule = new SimpleModule();
    //     simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
    //     simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
    //     DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    //     simpleModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
    //     simpleModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));
    //     simpleModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
    //     objectMapper.registerModule(simpleModule);
    //
    //     converter.setObjectMapper(objectMapper);
    //     converters.add(1, converter);
    // }


    /**
     * 使用此方法, 以下 spring-boot: jackson时间格式化 配置 将会失效
     * spring.jackson.time-zone=GMT+8
     * spring.jackson.date-format=yyyy-MM-dd HH:mm:ss
     * 原因: 会覆盖 @EnableAutoConfiguration 关于 WebMvcAutoConfiguration 的配置
     */
    // @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        ObjectMapper objectMapper = converter.getObjectMapper();
        SimpleDateFormat dateFormat = new SimpleDateFormat(DatePattern.NORM_DATETIME_PATTERN) {
            // 根据实际业务支持各种复杂格式的日期字符串。
            @Override
            public Date parse(String source) {
                boolean numberMatch = source.matches("^[0-9]*$");
                if (numberMatch) {
                    return new Date(Long.parseLong(source));
                }
                boolean halfDateMatch = source.matches("^\\d{4}-\\d{2}-\\d{2}$");
                if (halfDateMatch) {
                    return DateUtil.parse(source, DatePattern.NORM_DATE_PATTERN);
                }
                boolean fullDateMatch = source.matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$");
                if (fullDateMatch) {
                    return DateUtil.parse(source, DatePattern.NORM_DATETIME_PATTERN);
                }
                boolean allDateMath = source.matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}$");
                if (allDateMath) {
                    return DateUtil.parse(source, DatePattern.NORM_DATETIME_MS_PATTERN);
                }
                try {
                    return super.parse(source);// 支持解析指定pattern类型。
                } catch (ParseException e) {
                    throw new BusinessException(ErrorEnum.DATE_PARSE_ERROR, ErrorEnum.DATE_PARSE_ERROR.getMessage());
                }
            }
        };

        // 时间格式化
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setDateFormat(dateFormat);
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
        // 对于空的对象转json的时候不抛出错误
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        // 禁用遇到未知属性抛出异常
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);

        // 将Long序列成String
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        objectMapper.registerModule(simpleModule);
        // 设置格式化内容
        converter.setObjectMapper(objectMapper);
        converters.add(1, converter);
    }
}
