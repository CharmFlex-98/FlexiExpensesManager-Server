package com.charmflex.app.flexiexpensesmanager.remote_config.models

enum class RemoteConfigScene {
    HOME, REFERRAL, TAG
}


data class RemoteConfigAnnouncementRequest(
    val scene: RemoteConfigScene
)