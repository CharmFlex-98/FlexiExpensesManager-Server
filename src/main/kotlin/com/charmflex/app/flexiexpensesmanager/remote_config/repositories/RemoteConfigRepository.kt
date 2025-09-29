package com.charmflex.app.flexiexpensesmanager.remote_config.repositories

import com.charmflex.app.flexiexpensesmanager.remote_config.models.RemoteConfigAnnouncementResponse
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Repository
import java.io.BufferedReader

@Repository
final class RemoteConfigRepository(
    @Value("\${remote-config.scene-announcement.path}")
    private val remoteConfigFilePath: String,
    private val resourceLoader: ResourceLoader
) {
    @Volatile
    var announcementCache: List<RemoteConfigAnnouncementResponse>? = null
        private set
    fun setRemoteConfigAnnouncementResponse(remoteConfigAnnouncementResponse: List<RemoteConfigAnnouncementResponse>) {
        announcementCache = remoteConfigAnnouncementResponse
    }
}