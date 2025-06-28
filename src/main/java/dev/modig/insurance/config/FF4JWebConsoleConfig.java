package dev.modig.insurance.config;

import org.ff4j.FF4j;
import org.ff4j.web.FF4jDispatcherServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FF4JWebConsoleConfig {

    @Bean
    public ServletRegistrationBean<FF4jDispatcherServlet> ff4jServletRegistrationBean(FF4j ff4j) {
        FF4jDispatcherServlet servlet = new FF4jDispatcherServlet();
        servlet.setFf4j(ff4j);
        ServletRegistrationBean<FF4jDispatcherServlet> bean =
                new ServletRegistrationBean<>(servlet, "/ff4j-web-console/*");
        bean.setName("ff4jConsoleServlet");
        return bean;
    }
}
