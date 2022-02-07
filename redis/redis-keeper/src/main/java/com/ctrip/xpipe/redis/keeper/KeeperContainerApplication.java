package com.ctrip.xpipe.redis.keeper;

import com.ctrip.xpipe.api.lifecycle.ComponentRegistry;
import com.ctrip.xpipe.lifecycle.CreatedComponentRedistry;
import com.ctrip.xpipe.lifecycle.DefaultRegistry;
import com.ctrip.xpipe.lifecycle.SpringComponentRegistry;
import com.ctrip.xpipe.redis.keeper.container.ComponentRegistryHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@SpringBootApplication
public class KeeperContainerApplication {
	
	private static Logger logger = LoggerFactory.getLogger(KeeperContainerApplication.class);
	
    public static void main(String[] args) throws Exception {
    	
        SpringApplication application = new SpringApplication(KeeperContainerApplication.class);
		/* 关闭spring容器的优雅关机
		 * spring容器本身有优雅关机， 应用程序也有优雅关机， 但是在真正关机的时候， 他们谁先执行是不受控制的，
		 * 可能在应用关闭的时候， 应用程序优雅关机中，还需要spring容器中的bean, 如果此时spring先执行了， 应用程序就会报错。
		 * 所以这里取消了spring容器的优雅挂机。保留应用程序的， 可以在应用程序中手动调用spring的优雅关机
		 */
		application.setRegisterShutdownHook(false);
		// 容器启动
        final ConfigurableApplicationContext context = application.run(args);
        
        final ComponentRegistry registry = initComponentRegistry(context);

		// 添加钩子， 实现优雅停机
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				try {
					logger.info("[run][shutdown][stop]");
					registry.stop();
				} catch (Exception e) {
					logger.error("[run][shutdown][stop]", e);
				}
				try {
					logger.info("[run][shutdown][dispose]");
					registry.dispose();
				} catch (Exception e) {
					logger.error("[run][shutdown][dispose]", e);
				}
				
				try {
					logger.info("[run][shutdown][destroy]");
					registry.destroy();
				} catch (Exception e) {
					logger.error("[run][shutdown][destroy]", e);
				}
			}
		}));

    }

    private static ComponentRegistry initComponentRegistry(ConfigurableApplicationContext context) throws Exception {

		// 默认注册中心
        final ComponentRegistry registry = new DefaultRegistry(new CreatedComponentRedistry(),
                new SpringComponentRegistry(context));

		// 注册中心 也受生命周期的影响， 所以这里初始化，内部初始化了CreatedComponentRedistry SpringComponentRegistry
		registry.initialize();
		// 启动 ， 内部启动了CreatedComponentRedistry SpringComponentRegistry
        registry.start();
        ComponentRegistryHolder.initializeRegistry(registry);
        return registry;
    }
}
