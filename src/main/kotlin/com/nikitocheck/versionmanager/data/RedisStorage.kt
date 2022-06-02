package com.nikitocheck.versionmanager.data

import com.nikitocheck.versionmanager.VersionsInfo
import com.nikitocheck.versionmanager.domain.VersionStorage
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class RedisStorage(
        private val redisTemplate: RedisTemplate<String, String>
) : VersionStorage {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val maxIdKey = "maxSystemId"

    override fun getLatest(): VersionsInfo? {
        logger.debug("Executing getLatest()")

        val maxId = redisTemplate.opsForValue().get(maxIdKey) ?: return null

        return redisTemplate.opsForValue().get(maxId)
                ?.let { Json.decodeFromString<VersionsInfo>(it) }
    }

    override fun getVersions(systemVersion: Long): VersionsInfo? {
        logger.debug("Executing getVersion(). systemVersion =$systemVersion")
        return redisTemplate.opsForValue().get(systemVersion(systemVersion))
                ?.let { Json.decodeFromString<VersionsInfo>(it) }
    }

    override fun insert(info: VersionsInfo) {
        logger.debug("Executing insert() : info=$info")
        val systemIdKey = systemVersion(info.systemVersion)

        try {
            redisTemplate.execute {
                redisTemplate.multi()
                redisTemplate.opsForValue().set(systemIdKey, Json.encodeToString(info))
                redisTemplate.opsForValue().set(maxIdKey, systemIdKey)
                logger.info("Updated system version=${info.systemVersion}")
            }
        } catch (e: Throwable) {
            logger.error("Error occurred during insert. id=$systemIdKey.", e)
            redisTemplate.discard()
        }
        logger.debug("Finished insert(): info=$info")

    }

    private fun systemVersion(systemId: Long): String {
        return "systemVersion:$systemId"
    }

}