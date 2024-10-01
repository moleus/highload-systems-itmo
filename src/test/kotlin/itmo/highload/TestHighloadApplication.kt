package itmo.highload

import org.springframework.boot.fromApplication

fun main(args: Array<String>) {
  fromApplication<HighloadApplication>().run(*args)
}
