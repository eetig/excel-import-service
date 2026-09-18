package org.example.excel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient // 开启Nacos注册
public class ExcelImportApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExcelImportApplication.class, args);
    }
}
