package itmo.highload

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class EurekaServerApplication
@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<EurekaServerApplication>(*args)
}
