package com.utils;

import com.consts.CommonConstants;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

public class I18nLocaleResolver implements LocaleResolver {
    @Override
    public Locale resolveLocale(HttpServletRequest httpServletRequest) {
        // 获取请求中的语言参数
        String language = httpServletRequest.getHeader(CommonConstants.LANGUAGE_HEADER);
        Locale locale;
        if (StringUtils.hasText(language)) {
            // 如果请求头中携带了国际化的参数，创建对应的 Locale 对象
            locale = new Locale(language);
        } else {
            // 如果没有，使用默认的 Locale 对象（根据主机的语言环境生成一个 Locale ）。
            locale = Locale.getDefault();
        }
        return locale;
    }

    @Override
    public void setLocale(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Locale locale) {
    }
}
