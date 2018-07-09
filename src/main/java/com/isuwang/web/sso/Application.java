package com.isuwang.web.sso;


import com.ihyht.basic.platform.cache.FrameworkCacheConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.context.annotation.Import;


@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
@Import({
        FrameworkCacheConfig.class
})
public class Application {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }

}
