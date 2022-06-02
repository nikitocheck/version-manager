package com.nikitocheck.versionmanager

@kotlinx.serialization.Serializable
data class VersionsInfo(val systemVersion: Long, val versions: Map<String, Long>)