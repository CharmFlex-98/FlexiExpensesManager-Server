package com.charmflex.app.flexiexpensesmanager.remote_config.models


enum class IconType {
    ANNOUNCEMENT, COMING_SOON, IN_PROGRESS
}

enum class ActionType {
    CLOSE, UPDATE_AT_STORE, BACK
}

data class RemoteConfigAnnouncementResponse(
    val version: Int,
    val scene: RemoteConfigScene,
    val title: String,
    val subtitle: String,
    val label: String,
    val closable: Boolean,
    val iconType: IconType,
    val actionType: ActionType,
    val allowNotShowAgain: Boolean,
    val show: Boolean
)

data class RemoteConfigAnnouncementJson(
    val version: Int,
    val scene: RemoteConfigScene,
    val title: String,
    val subtitle: String,
    val label: String,
    val closable: Boolean,
    val iconType: IconType,
    val actionType: ActionType,
    val show: Boolean,
    val allowNotShowAgain: Boolean,
    val target: String
)