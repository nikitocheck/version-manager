package com.nikitocheck.versionmanager.controller

import com.nikitocheck.versionmanager.VersionService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class VersionsController(
        private val versionService: VersionService
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostMapping("deploy")
    fun updateVersion(@RequestBody body: DeployRequestBody): Long {
        logger.info("Start processing deploy request $body")
        val systemVersion = versionService.updateServiceVersion(body.name, body.version)
        logger.info("Finish processing deploy request $body systemVersion=$systemVersion")

        return systemVersion
    }

    @GetMapping("services")
    fun getVersions(
            @RequestParam("systemVersion") systemVersion: Long
    ): ResponseEntity<*> {
        val versions = versionService.getVersions(systemVersion)
                ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Version $systemVersion doesn't exist")

        val responseBody = versions.entries.map { (serviceName, version) -> ServiceVersionDto(serviceName, version) }

        return ResponseEntity.ok(responseBody)
    }

}