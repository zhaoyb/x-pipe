package com.ctrip.xpipe.redis.console.controller.config;

import com.ctrip.xpipe.redis.console.resources.MetaCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 *  spring mvc 拦截器配置
 *
 * @author wenchao.meng
 *         <p>
 *         Apr 06, 2017
 */
@Component
public class MvcConfig extends WebMvcConfigurerAdapter{

    @Autowired
    private MetaCache metaCache;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 日志拦截器，打印请求-响应日志
        registry.addInterceptor(new LogInterceptor());
        // 类型检查拦截器
        registry.addInterceptor(new ClusterCheckInterceptor(metaCache));
    }

}
