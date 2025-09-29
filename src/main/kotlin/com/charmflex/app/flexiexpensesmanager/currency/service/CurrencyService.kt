package com.charmflex.app.flexiexpensesmanager.currency.service

import com.charmflex.app.flexiexpensesmanager.currency.client.CurrencyRateResponse
import com.charmflex.app.flexiexpensesmanager.currency.repository.CurrencyRepository
import org.springframework.stereotype.Service

@Service
class CurrencyService(
    private val currencyRepository: CurrencyRepository
) {
    fun getCurrencyRate(): CurrencyRateResponse {
        return currencyRepository.getCurrencyRate()
    }
}