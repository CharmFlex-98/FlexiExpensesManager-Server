package com.charmflex.app.flexiexpensesmanager.remote_config.services

import com.charmflex.app.flexiexpensesmanager.remote_config.models.RemoteConfigAnnouncementResponse
import com.charmflex.app.flexiexpensesmanager.remote_config.models.RemoteConfigScene
import com.charmflex.app.flexiexpensesmanager.remote_config.repositories.RemoteConfigRepository
import org.springframework.cglib.core.Local
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.Locale

@Service
class RemoteConfigService(
    private val remoteConfigRepository: RemoteConfigRepository,
    private val messageSource: MessageSource
) {
    fun getAnnouncement(scene: RemoteConfigScene, localeKey: String?): RemoteConfigAnnouncementResponse? {
        val result = remoteConfigRepository.announcementCache
        val res = result?.first { it.scene == scene }

        if (res == null) return null

        val locale = when (localeKey) {
            "zh_CN" -> Locale.CHINESE
            "zh_TW" -> Locale.TAIWAN
            "zh_HK" -> Locale("zh", "HK")
            "ja" -> Locale.JAPANESE
            "ko" -> Locale.KOREAN
            else -> Locale.US
        }

        return RemoteConfigAnnouncementResponse(
            scene = res.scene,
            title = messageSource.getMessage(res.title, null, locale),
            subtitle =  messageSource.getMessage(res.subtitle, null, locale),
            label = messageSource.getMessage(res.label, null, locale),
            closable = res.closable,
            iconType = res.iconType,
            actionType = res.actionType,
            show = res.show
        )
    }
}