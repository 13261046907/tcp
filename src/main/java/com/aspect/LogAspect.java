package com.aspect;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
@Aspect
@Slf4j
public class LogAspect {
    @Pointcut("execution(* com.insshopy.agents.controller..*.*(..))")
    public void controller() {
    }

    @Around("controller()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long start = System.currentTimeMillis();
        HttpServletRequest request = getRequest();
        String url = request.getMethod() + " " + request.getRequestURI();
        Object requestParams = getMethodArgs(point);
        log.info("开始请求 => URL[{}],参数:[{}]", url, requestParams);

        Object result = point.proceed();

        long endTime = System.currentTimeMillis();
        String response = handlerResult(result);
        log.info("结束请求 => URL[{}],耗时:[{}]毫秒,response:[{}]", url, endTime - start,response);
        return result;
    }

    private String getMethodArgs(JoinPoint point) {
        Object[] args = point.getArgs();
        if (args == null || args.length == 0) {
            return "";
        }
        try {
            Map<String, Object> params = new HashMap<>();
            String[] parameterNames = ((MethodSignature) point.getSignature()).getParameterNames();
            for (int i = 0; i < parameterNames.length; i++) {
                Object arg = args[i];
                // 过滤不能转换成JSON的参数
                if ((arg instanceof ServletRequest) || (arg instanceof ServletResponse)) {
                    continue;
                } else if ((arg instanceof MultipartFile)) {
                    MultipartFile multipartFile = (MultipartFile) arg;
                    arg = MessageFormat.format("文件名: {0}, 大小: {1}", multipartFile.getOriginalFilename(), multipartFile.getSize());
                }
                params.put(parameterNames[i], arg);
            }
            return JSONObject.toJSONString(params);
        } catch (Exception e) {
            log.error("接口出入参日志打印切面处理请求参数异常", e);
        }
        return Arrays.toString(args);
    }


    private Object getRequestParams(ProceedingJoinPoint joinPoint, HttpServletRequest request) throws UnsupportedEncodingException {
        Object[] args = joinPoint.getArgs();
        Object params = null;
        String queryString = request.getQueryString();
        String method = request.getMethod();
        if (args.length > 0) {
            // 有body的接口类型，这时候要排除HttpServletRequest request, HttpServletResponse response作为接口方法参数
            if ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method)) {
                int length = args.length;
                int index = 0;
                Object object = null;
                while (index < length) {
                    Object o = args[index];
                    index++;
                    if (o instanceof HttpServletRequest || o instanceof HttpServletResponse) {
                        continue;
                    } else {
                        object = o;
                        break;
                    }
                }
                if (object instanceof MultipartFile) {
                    MultipartFile multipartFile = (MultipartFile) object;
                    params = MessageFormat.format("文件名: {0}, 大小: {1}", multipartFile.getOriginalFilename(), multipartFile.getSize());
                } else {
                    params = object;
                }
                // 方法为get时，当接口参数为路径参数，那么此时queryString为null
            } else if ("GET".equals(method) && StrUtil.isNotBlank(queryString)) {
                params = URLDecoder.decode(queryString, "utf-8");
            }
        }
        return params;
    }


    private HttpServletRequest getRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        return request;
    }

    /**
     * 返回结果简单处理
     * 1）把返回结果转成String，方便输出。
     * 2）返回结果太长则截取（最多3072个字符），方便展示。
     *
     * @param result 原方法调用的返回结果
     * @return 处理后的
     */
    private String handlerResult(Object result) {
        if (result == null) {
            return null;
        }
        String resultStr;
        try {
            if (result instanceof String) {
                resultStr = (String) result;
            } else {
                resultStr = JSONObject.toJSONString(result);// 如果返回结果非String类型，转换成JSON格式的字符串
            }

            if (resultStr.length() > 3072) {
                resultStr = resultStr.substring(0, 3072);
            }
        } catch (Exception e) {
            resultStr = result.toString();
            log.error("接口出入参日志打印切面处理返回参数异常", e);
        }
        return resultStr;
    }
}
