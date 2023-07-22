package com.grapecity.reportserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GCReportServerApplication {

    public static void main(String[] args) {
        initBoot();
        SpringApplication.run(GCReportServerApplication.class, args);
    }
    private static void initBoot() {
        System.setProperty("file.encoding", "UTF-8");
    }

}
