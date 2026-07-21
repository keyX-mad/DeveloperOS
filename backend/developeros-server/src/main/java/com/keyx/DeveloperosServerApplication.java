package com.keyx;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * 应用启动入口
 * <p>
 * @SpringBootApplication 默认扫描当前包（com.keyx）及其所有子包。
 * MyBatis-Plus 的 Mapper 扫描由 config.MybatisPlusConfig 统一管理。
 */
@SpringBootApplication
public class DeveloperosServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeveloperosServerApplication.class, args);
    }
}
