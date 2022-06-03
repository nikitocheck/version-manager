package com.nikitocheck.versionmanager

import com.nikitocheck.versionmanager.domain.VersionStorage
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference


internal class VersionServiceTest {

    @Test
    fun `given systemVersion exists should return versions`() {
        //given
        val systemVersion = 10L
        val serviceName = "ServiceA"
        val serviceVersion = 5L
        val versionsInfo = VersionsInfo(systemVersion, mapOf(serviceName to serviceVersion))
        val versionStorage = mockk<VersionStorage>()
        val versionService = VersionService(versionStorage)
        every { versionStorage.getVersions(any()) } returns versionsInfo

        //when
        val actual = versionService.getVersions(systemVersion)

        //then
        verify { versionStorage.getVersions(systemVersion) }
        Assertions.assertEquals(versionsInfo.versions, actual)
    }

    @Test
    fun `given system version doesn't exist then should return null`() {
        //given
        val systemVersion = 10L
        val versionStorage = mockk<VersionStorage>()
        val versionService = VersionService(versionStorage)
        every { versionStorage.getVersions(any()) } returns null

        //when
        val actual = versionService.getVersions(systemVersion)

        //then
        verify { versionStorage.getVersions(systemVersion) }
        Assertions.assertEquals(null, actual)
    }

    @Test
    fun `concurrent updates shouldn't be lost`() {
        //given
        val systemVersion = AtomicLong(0L)
        val versionsMap = AtomicReference(mapOf<String, Long>())

        val executor = Executors.newFixedThreadPool(2)
        val versionStorage = mockk<VersionStorage>()
        val versionService = VersionService(versionStorage)

        every { versionStorage.getLatest() } answers {
            VersionsInfo(systemVersion.get(), buildMap { putAll(versionsMap.get()) })
        }
        every { versionStorage.insert(any()) } answers {
            val versionsInfo = it.invocation.args[0] as VersionsInfo
            Thread.sleep(1000)
            systemVersion.set(versionsInfo.systemVersion)
            versionsMap.set(versionsInfo.versions)
        }

        val expectedFinalSystemVersion = 5L
        val expectedFinalMap = mapOf(
                "service1" to 1L,
                "service2" to 2L,
                "service3" to 1L,
                "service4" to 2L,
                "service5" to 2L,
        )
        val updates = listOf("service1" to 1L, "service2" to 2L, "service3" to 1L, "service4" to 2L, "service5" to 2L)
                .map { (s, v) ->
                    Runnable {
                        versionService.updateServiceVersion(s, v)
                    }
                }

        //when
        updates.map(executor::submit).forEach { it.get() }

        val latest = versionStorage.getLatest()!!

        //then
        Assertions.assertEquals(expectedFinalSystemVersion, latest.systemVersion)
        Assertions.assertEquals(expectedFinalMap, latest.versions)

    }
}