package com.charmflex.app.flexiexpensesmanager.remote_config.models

import kotlinx.serialization.Serializable

data class AdsInfoJson(
    val scene: AdsScene,
    val adsType: AdsType,
    val togglePhase: Int,
    val provider: AdsProvider
)

data class AdsConfigResponse(
    val adsInfos: List<AdsInfo>
)
data class AdsInfo(
    val scene: AdsScene,
    val adsType: AdsType,
    val togglePhase: Int, // 1 to 100
    val provider: AdsProvider
)

enum class AdsScene {
    HOME, SETTING, NEW_TRANSACTION
}

enum class AdsType {
    BANNER, INTERSTITIAL, VIDEO
}

enum class AdsProvider {
    AD_MOB, APPYLAR
}