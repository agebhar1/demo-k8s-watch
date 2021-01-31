package com.github.agebhar1.demo

import org.springframework.boot.Banner.Mode.OFF
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class Application

fun main(args: Array<String>) {
  runApplication<Application>(*args) { setBannerMode(OFF) }
}
