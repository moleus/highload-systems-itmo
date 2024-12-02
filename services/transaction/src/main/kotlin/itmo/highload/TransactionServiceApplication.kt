package itmo.highload

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.hystrix.EnableHystrix
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.web.reactive.config.EnableWebFlux
import reactivefeign.spring.config.EnableReactiveFeignClients

@EnableWebFlux
@SpringBootApplication
@EnableFeignClients
@EnableReactiveFeignClients
@EnableHystrix
class TransactionServiceApplication
@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<TransactionServiceApplication>(*args)
}
