package com.kirchnersolutions.picenter.backup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PiCenterBackupServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PiCenterBackupServiceApplication.class, args);
    }

}
