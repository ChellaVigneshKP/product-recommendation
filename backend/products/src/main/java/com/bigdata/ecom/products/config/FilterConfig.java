package com.bigdata.ecom.products.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<UrlDecodeFilter> loggingFilter() {
        FilterRegistrationBean<UrlDecodeFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new UrlDecodeFilter());
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }
}
