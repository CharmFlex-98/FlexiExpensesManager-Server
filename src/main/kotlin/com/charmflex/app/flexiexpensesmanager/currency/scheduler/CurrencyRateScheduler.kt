package com.charmflex.app.flexiexpensesmanager.currency.scheduler

import com.charmflex.app.flexiexpensesmanager.currency.client.CurrencyProxy
import com.charmflex.app.flexiexpensesmanager.currency.client.CurrencyRateResponse
import com.charmflex.app.flexiexpensesmanager.currency.repository.CurrencyRepository
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
@EnableScheduling
class CurrencyRateScheduler(
    private val currencyProxy: CurrencyProxy,
    private val currencyRepository: CurrencyRepository
) {
    @Volatile
    private var cache: CurrencyRateResponse? = null

    @Scheduled(fixedRate = 3600000)
    fun fetchLatestCurrencyRate() {
        try {
            val res = currencyProxy.latestCurrencyRate()
            currencyRepository.updateCurrencyRate(res)
        } catch (exception: Exception) {
            println("error in CurrencyRateScheduler: ${exception.message}")
        }
    }
}