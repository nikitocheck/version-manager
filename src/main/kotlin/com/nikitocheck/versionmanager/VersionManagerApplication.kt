package com.nikitocheck.versionmanager

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class VersionManagerApplication

fun main(args: Array<String>) {
	runApplication<VersionManagerApplication>(*args)
}
