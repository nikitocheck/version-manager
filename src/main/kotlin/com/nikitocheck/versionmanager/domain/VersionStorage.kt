package com.nikitocheck.versionmanager.domain

import com.nikitocheck.versionmanager.VersionsInfo

interface VersionStorage {
    fun getLatest(): VersionsInfo?
    fun getVersions(systemVersion: Long): VersionsInfo?
    fun insert(info: VersionsInfo)
}