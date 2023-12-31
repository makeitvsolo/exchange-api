package com.makeitvsolo.exchangeapi.service.impl;

import com.makeitvsolo.exchangeapi.datasource.CurrencyRepository;
import com.makeitvsolo.exchangeapi.domain.Currency;
import com.makeitvsolo.exchangeapi.domain.mapping.MappedFromCurrency;
import com.makeitvsolo.exchangeapi.service.CurrencyService;
import com.makeitvsolo.exchangeapi.service.dto.currency.CreateCurrencyDto;
import com.makeitvsolo.exchangeapi.service.dto.currency.CurrencyDto;
import com.makeitvsolo.exchangeapi.service.dto.currency.CurrencyListDto;
import com.makeitvsolo.exchangeapi.service.exception.currency.CurrencyAlreadyExistsException;
import com.makeitvsolo.exchangeapi.service.exception.currency.CurrencyNotFoundException;

public final class BaseCurrencyService implements CurrencyService {
    private final CurrencyRepository repository;
    private final MappedFromCurrency<CurrencyDto> mapper;

    public BaseCurrencyService(
            CurrencyRepository repository, MappedFromCurrency<CurrencyDto> mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public CurrencyDto create(CreateCurrencyDto payload) {
        if (repository.fetchByCode(payload.code()).isPresent()) {
            throw new CurrencyAlreadyExistsException(payload.code());
        }

        var currency = Currency.create(payload.code(), payload.fullName(), payload.sign());

        repository.save(currency);

        return currency.map(mapper);
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
