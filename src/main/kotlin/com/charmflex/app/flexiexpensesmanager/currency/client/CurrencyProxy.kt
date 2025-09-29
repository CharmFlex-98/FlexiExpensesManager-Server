package com.charmflex.app.flexiexpensesmanager.currency.client

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping

@FeignClient(
    name = "currency",
    url = "https://api.fxratesapi.com"
)
interface CurrencyProxy {
    @GetMapping("/latest")
    fun latestCurrencyRate(): CurrencyRateResponse
}


data class CurrencyRateResponse(
    val success: Boolean,
    val terms: String,
    val privacy: String,
    val timestamp: Long,
    val date: String,
    val base: String,
    val rates: Map<String, Double>
)