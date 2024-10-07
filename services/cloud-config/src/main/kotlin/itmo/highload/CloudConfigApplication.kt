package itmo.highload

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.config.server.EnableConfigServer

@SpringBootApplication
@EnableConfigServer
class CloudConfigApplication
@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<CloudConfigApplication>(*args)
}
