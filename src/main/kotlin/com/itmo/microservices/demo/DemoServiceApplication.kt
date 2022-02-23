package com.itmo.microservices.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import io.prometheus.client.spring.boot.EnablePrometheusEndpoint;

@SpringBootApplication
@EnablePrometheusEndpoint
class DemoServiceApplication

fun main(args: Array<String>) {
    runApplication<DemoServiceApplication>(*args)
}