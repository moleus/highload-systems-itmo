package itmo.highload

import org.springframework.boot.fromApplication
import org.springframework.boot.with

fun main(args: Array<String>) {
  fromApplication<HighloadApplication>().run(*args)
}
