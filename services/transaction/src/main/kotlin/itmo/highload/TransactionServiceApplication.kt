package itmo.highload

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.reactive.config.EnableWebFlux

@EnableWebFlux
@SpringBootApplication
class TransactionServiceApplication
@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<TransactionServiceApplication>(*args)
}
