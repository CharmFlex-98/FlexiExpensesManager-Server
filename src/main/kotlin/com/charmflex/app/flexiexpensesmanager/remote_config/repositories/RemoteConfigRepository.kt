package com.charmflex.app.flexiexpensesmanager.remote_config.repositories

import com.charmflex.app.flexiexpensesmanager.remote_config.models.RemoteConfigAnnouncementJson
import com.charmflex.app.flexiexpensesmanager.remote_config.models.RemoteConfigAnnouncementResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Repository

@Repository
final class RemoteConfigRepository(
    @Value("\${remote-config.scene-announcement.path}")
    private val remoteConfigFilePath: String,
    private val resourceLoader: ResourceLoader
) {
    @Volatile
    var announcementCache: List<RemoteConfigAnnouncementJson>? = null
        private set
    fun setRemoteConfigAnnouncementResponse(remoteConfigAnnouncementJson: List<RemoteConfigAnnouncementJson>) {
        announcementCache = remoteConfigAnnouncementJson
    }
}