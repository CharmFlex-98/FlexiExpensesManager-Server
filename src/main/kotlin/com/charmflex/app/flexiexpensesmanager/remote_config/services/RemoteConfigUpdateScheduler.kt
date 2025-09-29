package com.charmflex.app.flexiexpensesmanager.remote_config.services

import com.charmflex.app.flexiexpensesmanager.remote_config.models.ActionType
import com.charmflex.app.flexiexpensesmanager.remote_config.models.IconType
import com.charmflex.app.flexiexpensesmanager.remote_config.models.RemoteConfigAnnouncementResponse
import com.charmflex.app.flexiexpensesmanager.remote_config.models.RemoteConfigScene
import com.charmflex.app.flexiexpensesmanager.remote_config.repositories.RemoteConfigRepository
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.ResourceLoader
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.io.BufferedReader


@Service
class RemoteConfigUpdateScheduler(
    @Value("\${remote-config.scene-announcement.path}")
    private val remoteConfigFilePath: String,
    private val resourceLoader: ResourceLoader,
    private val repository: RemoteConfigRepository
) {

    @Scheduled(fixedRate = 60_000)
    fun updateScene() {
        val jsonRes = FileSystemResource(remoteConfigFilePath)
        val text = jsonRes.inputStream.bufferedReader().use(BufferedReader::readText)

        val scenes: List<JsonObject> = Json.decodeFromString(text)

        val res = scenes.map {
            val jsonObject = it as? JsonObject
            jsonObject?.let {
                RemoteConfigAnnouncementResponse(
                    title = (it["title"] as JsonPrimitive).content,
                    subtitle = (it.get("subtitle") as JsonPrimitive).content,
                    label = (it.get("label") as JsonPrimitive).content,
                    actionType = ActionType.valueOf((it["actionType"] as JsonPrimitive).content),
                    closable = (it.get("closable") as JsonPrimitive).content.toBoolean(),
                    iconType = IconType.valueOf((it.get("iconType") as JsonPrimitive).content),
                    scene = RemoteConfigScene.valueOf((it["scene"] as JsonPrimitive).content),
                    show = (it.get("show") as JsonPrimitive).content.toBoolean()
                )
            }
        }.filterNotNull()

        println(res)

        repository.setRemoteConfigAnnouncementResponse(res)
    }
}