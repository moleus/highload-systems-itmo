package org.example.highload

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
	fromApplication<HighloadApplication>().with(TestcontainersConfiguration::class).run(*args)
}
