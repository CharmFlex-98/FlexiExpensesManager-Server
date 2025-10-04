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
    fun getAnnouncement(scene: RemoteConfigScene, localeKey: String, appVersion: String): RemoteConfigAnnouncementResponse? {
        val result = remoteConfigRepository.announcementCache
        val appVer = appVersion.split("-")[0]
        val res = result?.firstOrNull { it.scene == scene && matchVersion(appVer, it.target) }

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
            show = res.show,
            version = res.version,
            allowNotShowAgain = res.allowNotShowAgain
        )
    }

    private fun matchVersion(appVersion: String, target: String): Boolean {
        if (target.isEmpty()) {
            return true
        }

        val splits = target.split('|')

        if (splits.isEmpty()) return false

        if (splits.size == 1) {
            return appVersion == splits[0]
        }

        if (splits[0].isEmpty()) {
            // e.g. |1.0.1
            val res = compareVersion(appVersion, splits[1])
            return res == -1
        }

        if (splits[1].isEmpty()) {
            // e.g. 1.0.1|
            val res = compareVersion(appVersion, splits[0])
            return res == 0 || res == 1
        }

        return false
    }

    private fun compareVersion(appVersion: String, target: String): Int {
        val appVersionTotal = appVersion.split(".").map { it.toInt() }.reduce { acc, item ->  acc * 10 + item }
        val targetTotal = target.split(".").map { it.toInt() }.reduce { acc, item ->  acc * 10 + item }

        if (appVersionTotal < targetTotal) return -1
        if (appVersionTotal == targetTotal) return 0
        return 1
    }
}