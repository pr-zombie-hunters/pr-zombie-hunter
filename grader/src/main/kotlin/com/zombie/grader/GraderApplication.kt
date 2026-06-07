package com.zombie.grader

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GraderApplication

fun main(args: Array<String>) {
	runApplication<GraderApplication>(*args)
}
