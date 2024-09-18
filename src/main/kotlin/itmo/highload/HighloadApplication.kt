package itmo.highload

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class HighloadApplication


@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<HighloadApplication>(*args)
}
