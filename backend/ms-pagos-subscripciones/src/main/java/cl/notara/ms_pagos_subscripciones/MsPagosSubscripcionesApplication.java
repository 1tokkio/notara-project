package cl.notara.ms_pagos_subscripciones;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class MsPagosSubscripcionesApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsPagosSubscripcionesApplication.class, args);
    }
}
