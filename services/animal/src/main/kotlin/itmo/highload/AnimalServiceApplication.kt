package itmo.highload

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cloud.netflix.hystrix.EnableHystrix
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.web.reactive.config.EnableWebFlux
import reactivefeign.spring.config.EnableReactiveFeignClients

@EnableCaching
@EnableWebFlux
@EnableFeignClients
@EnableReactiveFeignClients
@EnableHystrix
@SpringBootApplication(
    exclude = [
        SecurityAutoConfiguration::class,
        UserDetailsServiceAutoConfiguration::class
    ]
)
class AnimalServiceApplication

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<AnimalServiceApplication>(*args)
}
