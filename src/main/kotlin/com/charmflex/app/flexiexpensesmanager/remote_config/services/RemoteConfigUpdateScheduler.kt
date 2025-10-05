package com.charmflex.app.flexiexpensesmanager.remote_config.services

import com.charmflex.app.flexiexpensesmanager.remote_config.models.*
import com.charmflex.app.flexiexpensesmanager.remote_config.repositories.RemoteConfigRepository
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.ResourceLoader
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.io.BufferedReader


@Service
class RemoteConfigUpdateScheduler(
    @Value("\${remote-config.scene-announcement.path}")
    private val announcementConfigFilePath: String,
    @Value("\${remote-config.ads-config.path}")
    private val adsConfigFilePath: String,
    private val repository: RemoteConfigRepository
) {

    @Scheduled(fixedRate = 60_000)
    fun updateScene() {
        val jsonRes = FileSystemResource(announcementConfigFilePath)
        val text = jsonRes.inputStream.bufferedReader().use(BufferedReader::readText)

        val scenes: List<JsonObject> = Json.decodeFromString(text)

        val res = scenes.map {
            val jsonObject = it as? JsonObject
            jsonObject?.let {
                RemoteConfigAnnouncementJson(
                    title = (it["title"] as JsonPrimitive).content,
                    subtitle = (it.get("subtitle") as JsonPrimitive).content,
                    label = (it.get("label") as JsonPrimitive).content,
                    actionType = ActionType.valueOf((it["actionType"] as JsonPrimitive).content),
                    closable = (it.get("closable") as JsonPrimitive).content.toBoolean(),
                    iconType = IconType.valueOf((it.get("iconType") as JsonPrimitive).content),
                    scene = RemoteConfigScene.valueOf((it["scene"] as JsonPrimitive).content),
                    show = (it.get("show") as JsonPrimitive).content.toBoolean(),
                    target = (it.get("target") as JsonPrimitive).content,
                    version = (it.get("version") as JsonPrimitive).content.toInt(),
                    allowNotShowAgain = (it.get("allowNotShowAgain") as JsonPrimitive).content.toBoolean()
                )
            }
        }.filterNotNull()

        repository.setRemoteConfigAnnouncementResponse(res)
    }

    @Scheduled(fixedRate = 60_000)
    fun updateAdsConfig() {
        val jsonRes = FileSystemResource(adsConfigFilePath)
        val text = jsonRes.inputStream.bufferedReader().use(BufferedReader::readText)

        val scenes: List<JsonObject> = Json.decodeFromString(text)

        val res = scenes.map {
            val jsonObject = it as? JsonObject
            jsonObject?.let {
                AdsInfo(
                    scene = AdsScene.valueOf((it.get("scene") as JsonPrimitive).content),
                    adsType = AdsType.valueOf((it.get("adsType") as JsonPrimitive).content),
                    togglePhase = (it.get("togglePhase") as JsonPrimitive).content.toInt(),
                    provider = AdsProvider.valueOf((it.get("provider") as JsonPrimitive).content)
                )
            }
        }.filterNotNull()

        repository.setAdsConfigResponse(AdsConfigResponse(res))
    }
}