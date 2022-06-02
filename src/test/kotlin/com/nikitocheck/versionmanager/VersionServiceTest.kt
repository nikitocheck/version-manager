package com.nikitocheck.versionmanager

import com.nikitocheck.versionmanager.domain.VersionStorage
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors


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
    fun `update version should work correct concurrently`() {
        //given
        var systemVersion = 0L
        var versionsMap = mapOf<String, Long>()

        val executor = Executors.newFixedThreadPool(2)
        val versionStorage = mockk<VersionStorage>()
        val versionService = VersionService(versionStorage)

        every { versionStorage.getLatest() } answers {
            VersionsInfo(systemVersion, buildMap { putAll(versionsMap) })
        }
        every { versionStorage.insert(any()) } answers {
            val versionsInfo = it.invocation.args[0] as VersionsInfo
            Thread.sleep(1000)
            systemVersion = versionsInfo.systemVersion
            versionsMap = versionsInfo.versions
        }

        val expectedFinalSystemVersion = 4L
        val expectedFinalMap = mapOf(
                "service1" to 1L,
                "service2" to 3L,
                "service3" to 1L
        )
        val updates = listOf("service1" to 1L, "service2" to 2L, "service3" to 1L, "service2" to 2L, "service2" to 3L)
                .map { (s, v) -> Runnable { versionService.updateServiceVersion(s, v) } }

        //when
        updates.map(executor::submit).forEach { it.get() }

        //then
        Assertions.assertEquals(expectedFinalSystemVersion, systemVersion)
        Assertions.assertEquals(expectedFinalMap, versionsMap)

    }
}