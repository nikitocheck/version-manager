package com.nikitocheck.versionmanager.controller

import com.nikitocheck.versionmanager.VersionService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

class VersionsControllerTest {

    @Test
    fun `given deploy request then should update service version`() {
        //given
        val newSystemVersion = 10L
        val serviceName = "ServiceA"
        val serviceVersion = 5L
        val versionsService = mockk<VersionService>()
        val controller = VersionsController(versionsService)
        every { versionsService.updateServiceVersion(any(), any()) } returns newSystemVersion
        //when
        val actual = controller.updateVersion(DeployRequestBody(serviceName, serviceVersion))
        //then
        Assertions.assertEquals(newSystemVersion, actual)
        verify { versionsService.updateServiceVersion(serviceName, serviceVersion) }
    }

    @Test
    fun `given system version exists then should return service versions`() {
        //given
        val systemVersion = 10L
        val serviceName = "ServiceA"
        val serviceVersion = 5L
        val versions = mapOf(serviceName to serviceVersion)
        val versionsService = mockk<VersionService>()
        val controller = VersionsController(versionsService)
        val expected = ResponseEntity.ok().body(listOf(ServiceVersionDto(serviceName, serviceVersion)))
        every { versionsService.getVersions(any()) } returns versions

        //when
        val actual = controller.getVersions(systemVersion)

        //then
        verify { versionsService.getVersions(systemVersion) }
        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun `given system version doesn't exist then should return not found response`() {
        //given
        val systemVersion = 10L
        val versionsService = mockk<VersionService>()
        val controller = VersionsController(versionsService)
        every { versionsService.getVersions(any()) } returns null

        //when
        val actual = controller.getVersions(systemVersion)

        //then
        verify { versionsService.getVersions(systemVersion) }
        Assertions.assertTrue(actual.statusCode == HttpStatus.NOT_FOUND)
    }
}