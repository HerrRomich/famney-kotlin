package io.github.herrromich.famoney.launcher

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = arrayOf("io.github.herrromich.famoney"))
class ServerLauncher

fun main(args: Array<String>) {
    runApplication<ServerLauncher>(*args)
}