package com.hrrm.famoney.launcher

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = arrayOf("com.hrrm.famoney"))
class ServerLauncher

fun main(args: Array<String>) {
    runApplication<ServerLauncher>(*args)
}