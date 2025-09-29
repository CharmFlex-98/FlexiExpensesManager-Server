package com.charmflex.app.flexiexpensesmanager

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableFeignClients(
	basePackages = ["com.charmflex.app.flexiexpensesmanager"]
)
class FlexiexpensesmanagerApplication

fun main(args: Array<String>) {
	runApplication<FlexiexpensesmanagerApplication>(*args)
}
