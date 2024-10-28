package itmo.highload

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.reactive.config.EnableWebFlux

@EnableWebFlux
@SpringBootApplication
class ImagesServiceApplication
@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<ImagesServiceApplication>(*args)
}
