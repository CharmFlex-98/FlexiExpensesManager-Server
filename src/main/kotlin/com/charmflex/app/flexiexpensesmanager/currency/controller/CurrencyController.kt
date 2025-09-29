package com.charmflex.app.flexiexpensesmanager.currency.controller

import com.charmflex.app.flexiexpensesmanager.core.interceptors.SignedResponse
import com.charmflex.app.flexiexpensesmanager.currency.client.CurrencyRateResponse
import com.charmflex.app.flexiexpensesmanager.currency.service.CurrencyService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/v1/currency")
class CurrencyController(
    private val currencyService: CurrencyService
) {

    @GetMapping("/latest")
    @ResponseStatus(HttpStatus.OK)
    fun latestCurrencyRate(): CurrencyRateResponse {
        return currencyService.getCurrencyRate()
    }
}