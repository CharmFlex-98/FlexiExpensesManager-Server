package com.charmflex.app.flexiexpensesmanager.remote_config

import com.charmflex.app.flexiexpensesmanager.core.exceptions.GenericException
import com.charmflex.app.flexiexpensesmanager.core.interceptors.SignedResponse
import com.charmflex.app.flexiexpensesmanager.remote_config.models.RemoteConfigAnnouncementRequest
import com.charmflex.app.flexiexpensesmanager.remote_config.models.RemoteConfigAnnouncementResponse
import com.charmflex.app.flexiexpensesmanager.remote_config.services.RemoteConfigService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("api/v1/remote-config")
class RemoteConfigController(
    private val remoteConfigService: RemoteConfigService
) {

    @PostMapping("/announcement")
    @ResponseStatus(HttpStatus.OK)
    @SignedResponse
    fun getSceneAnnouncement(
        @RequestBody remoteConfigAnnouncementRequest: RemoteConfigAnnouncementRequest,
        @RequestHeader appVersion: String,
        @RequestHeader("Accept-Language") language: String // or any header name
    ): RemoteConfigAnnouncementResponse {
        return remoteConfigService.getAnnouncement(remoteConfigAnnouncementRequest.scene, language, appVersion) ?: throw GenericException
    }
}