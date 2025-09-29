package com.charmflex.app.flexiexpensesmanager.currency.repository

import com.charmflex.app.flexiexpensesmanager.currency.client.CurrencyRateResponse
import org.springframework.stereotype.Repository

@Repository
class CurrencyRepository {
    private final var cache: CurrencyRateResponse? = null

    fun updateCurrencyRate(currencyRateResponse: CurrencyRateResponse) {
        cache = currencyRateResponse
    }

    fun getCurrencyRate(): CurrencyRateResponse {
        return cache!!
    }
}