package com.potatonetwork.happymemories

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HappyMemoriesBackendApplication

fun main(args: Array<String>) {
    runApplication<HappyMemoriesBackendApplication>(*args)
}
