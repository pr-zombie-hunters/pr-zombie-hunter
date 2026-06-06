package com.zombie.notifier

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class NotifierApplication

fun main(args: Array<String>) {
	runApplication<NotifierApplication>(*args)
}
