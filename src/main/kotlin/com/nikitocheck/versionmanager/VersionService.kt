package com.nikitocheck.versionmanager

import com.nikitocheck.versionmanager.domain.VersionStorage
import org.springframework.stereotype.Service
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@Service
class VersionService(
        private val repository: VersionStorage,
) {
    private val lock = ReentrantLock()

    fun updateServiceVersion(serviceName: String, serviceVersion: Long): Long = lock.withLock {
        val latest = repository.getLatest() ?: VersionsInfo(0, emptyMap())

        if (latest.versions[serviceName] == serviceVersion) {
            return@withLock latest.systemVersion
        }

        val newVersion = latest.systemVersion + 1
        repository.insert(VersionsInfo(systemVersion = newVersion, versions = HashMap(latest.versions).apply {
            put(serviceName, serviceVersion)
        }))
        return@withLock newVersion
    }

    fun getVersions(systemVersion: Long): Map<String, Long>? {
        return repository.getVersions(systemVersion)?.versions
    }
}