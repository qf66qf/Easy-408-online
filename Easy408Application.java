package com.easy408;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Easy408Application {
    public static void main(String[] args) {
        // 确保 headless 模式开启，避免某些环境下的图形界面报错
        System.setProperty("java.awt.headless", "false");
        SpringApplication.run(Easy408Application.class, args);
    }
}