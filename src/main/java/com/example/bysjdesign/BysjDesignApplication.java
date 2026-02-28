package com.example.bysjdesign;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.ComponentScan;

/**
 * 校园网用户行为分析与画像系统
 * 核心启动类
 */
@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {
        "com.example.bysjdesign.campus",
        "com.example.bysjdesign.config"
})
public class BysjDesignApplication {

    public static void main(String[] args) {
        SpringApplication.run(BysjDesignApplication.class, args);
        System.out.println("========================================");
        System.out.println("校园网用户行为分析与画像系统已启动");
        System.out.println("后端服务运行于: http://localhost:8080");
        System.out.println("API文档: http://localhost:8080/api/campus");
        System.out.println("========================================");
    }
}