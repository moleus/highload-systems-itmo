package itmo.highload

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CloudConfigApplication
@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<CloudConfigApplication>(*args)
}
