package com.handler;

import cn.hutool.core.util.ReUtil;
import com.utils.SpringUtils;
import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PathPatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 获取所有Url配置
 *
 * @author Lion Li
 */
@Data
@Component
public class AllUrlHandler implements InitializingBean {

    private static final Pattern PATTERN = Pattern.compile("\\{(.*?)\\}");

    private List<String> urls = new ArrayList<>();

    @Override
    public void afterPropertiesSet() {
        Set<String> set = new HashSet<>();
        RequestMappingHandlerMapping mapping = SpringUtils.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
        Map<RequestMappingInfo, HandlerMethod> map = mapping.getHandlerMethods();
        map.keySet().forEach(info -> {
            PathPatternsRequestCondition pathPatternsCondition = info.getPathPatternsCondition();
            // 检查 patternsCondition 是否为 null
            if (pathPatternsCondition != null) {
                // 获取注解上的路径，替代路径变量为 *
                pathPatternsCondition.getDirectPaths().forEach(url -> {
                    set.add(ReUtil.replaceAll(url, PATTERN, "*"));
                });
            }
        });
        urls.addAll(set);
    }

}
