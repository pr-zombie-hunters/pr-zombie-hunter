package com.zombie.gateway

import jakarta.servlet.http.HttpServletRequest
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate

@SpringBootApplication
class GatewayApplication

fun main(args: Array<String>) {
    runApplication<GatewayApplication>(*args)
}

@RestController
class ProxyController {

    private val restTemplate = RestTemplate()

    @RequestMapping("/collector/**")
    fun proxyCollector(
        request: HttpServletRequest,
        @RequestBody(required = false) body: ByteArray?,
    ): ResponseEntity<ByteArray> = proxy(request, body, "http://collector:8081", "/collector")

    @RequestMapping("/grader/**")
    fun proxyGrader(
        request: HttpServletRequest,
        @RequestBody(required = false) body: ByteArray?,
    ): ResponseEntity<ByteArray> = proxy(request, body, "http://grader:8082", "/grader")

    @RequestMapping("/notifier/**")
    fun proxyNotifier(
        request: HttpServletRequest,
        @RequestBody(required = false) body: ByteArray?,
    ): ResponseEntity<ByteArray> = proxy(request, body, "http://notifier:8083", "/notifier")

    @RequestMapping("/api/**")
    fun proxyApi(
        request: HttpServletRequest,
        @RequestBody(required = false) body: ByteArray?,
    ): ResponseEntity<ByteArray> = proxy(request, body, "http://api-service:8084", "")

    private fun proxy(
        request: HttpServletRequest,
        body: ByteArray?,
        targetBase: String,
        stripPrefix: String,
    ): ResponseEntity<ByteArray> {
        val path = request.requestURI.removePrefix(stripPrefix)
        val query = if (request.queryString != null) "?${request.queryString}" else ""
        val targetUrl = "$targetBase$path$query"

        val headers = HttpHeaders()
        request.headerNames.asIterator().forEach { name ->
            headers.set(name, request.getHeader(name))
        }

        val method = HttpMethod.valueOf(request.method)
        val entity = HttpEntity(body, headers)

        return restTemplate.exchange(targetUrl, method, entity, ByteArray::class.java)
    }
}
