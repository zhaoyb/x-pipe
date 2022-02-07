package com.ctrip.xpipe.redis.console;

import com.ctrip.xpipe.redis.console.healthcheck.HealthChecker;
import com.ctrip.xpipe.spring.AbstractProfile;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * spring boot 启动
 *
 * @author shyin
 *
 * Jul 28, 2016
 */
@SpringBootApplication
public class App {
	public static void main(String[] args){
		// 设置spring.profiles.active 环境变量
		System.setProperty("spring.profiles.active", AbstractProfile.PROFILE_NAME_PRODUCTION);
		// 设置 healthcheck.enable 变量
		System.setProperty(HealthChecker.ENABLED, "true");
		// spring 启动
		SpringApplication.run(App.class, args);
	}
}
