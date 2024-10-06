package itmo.highload

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.web.config.EnableSpringDataWebSupport

@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
@SpringBootApplication
class AdoptionServiceApplication

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<AdoptionServiceApplication>(*args)
}
