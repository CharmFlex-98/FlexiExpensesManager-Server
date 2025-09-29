package com.charmflex.app.flexiexpensesmanager.remote_config.services

import com.charmflex.app.flexiexpensesmanager.remote_config.models.RemoteConfigAnnouncementResponse
import com.charmflex.app.flexiexpensesmanager.remote_config.models.RemoteConfigScene
import com.charmflex.app.flexiexpensesmanager.remote_config.repositories.RemoteConfigRepository
import org.springframework.stereotype.Service

@Service
class RemoteConfigService(
    private val remoteConfigRepository: RemoteConfigRepository
) {
    fun getAnnouncement(scene: RemoteConfigScene): RemoteConfigAnnouncementResponse? {
        val result = remoteConfigRepository.announcementCache
        return result?.first { it.scene == scene }
    }
}