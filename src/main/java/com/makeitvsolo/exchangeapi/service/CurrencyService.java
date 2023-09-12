package com.makeitvsolo.exchangeapi.service;

import com.makeitvsolo.exchangeapi.core.unique.Unique;
import com.makeitvsolo.exchangeapi.datasource.CurrencyRepository;
import com.makeitvsolo.exchangeapi.domain.Currency;
import com.makeitvsolo.exchangeapi.domain.mapping.MappedFromCurrency;
import com.makeitvsolo.exchangeapi.service.dto.currency.CreateCurrencyDto;
import com.makeitvsolo.exchangeapi.service.dto.currency.CurrencyDto;
import com.makeitvsolo.exchangeapi.service.dto.currency.CurrencyListDto;
import com.makeitvsolo.exchangeapi.service.exception.currency.CurrencyAlreadyExistsException;
import com.makeitvsolo.exchangeapi.service.exception.currency.CurrencyNotFoundException;

import java.util.UUID;

public final class CurrencyService {
    private final CurrencyRepository repository;
    private final Unique<UUID> currencyId;
    private final MappedFromCurrency<CurrencyDto> mapper;

    public CurrencyService(
            CurrencyRepository repository, Unique<UUID> currencyId, MappedFromCurrency<CurrencyDto> mapper
    ) {
        this.repository = repository;
        this.currencyId = currencyId;
        this.mapper = mapper;
    }

    public void create(CreateCurrencyDto payload) {
        if (repository.fetchByCode(payload.code()).isPresent()) {
            throw new CurrencyAlreadyExistsException(payload.code());
        }

        var currency = Currency.create(currencyId, payload.code(), payload.fullName(), payload.sign());

        repository.save(currency);
    }

    public CurrencyDto byCode(String code) {
        return repository.fetchByCode(code)
                       .map(currency -> currency.map(mapper))
                       .orElseThrow(() -> new CurrencyNotFoundException(code));
    }

    public CurrencyListDto all() {
        var currencies = repository.fetchAll()
                                 .stream()
                                 .map(currency -> currency.map(mapper))
                                 .toList();

        return new CurrencyListDto(currencies);
    }
}