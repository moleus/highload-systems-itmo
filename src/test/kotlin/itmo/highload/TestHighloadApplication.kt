package itmo.highload

import org.springframework.boot.fromApplication
@Suppress("SpreadOperator")
fun main(args: Array<String>) {
  fromApplication<HighloadApplication>().run(*args)
}
