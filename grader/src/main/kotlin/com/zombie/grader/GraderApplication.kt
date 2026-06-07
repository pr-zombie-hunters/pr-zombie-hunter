package com.zombie.grader

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class GraderApplication

fun main(args: Array<String>) {
	runApplication<GraderApplication>(*args)
}
