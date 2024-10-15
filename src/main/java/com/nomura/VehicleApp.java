package com.nomura;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Properties;

@SpringBootApplication
public class VehicleApp {

    public static void main(String[] args) {

        Properties properties = System.getProperties();
        properties.setProperty("-DIGNITE_SKIP_CONFIGURATION_CONSISTENCY_CHECK","true");
        SpringApplication.run(VehicleApp.class, args);
    }


}
