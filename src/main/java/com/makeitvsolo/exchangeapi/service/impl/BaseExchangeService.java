package com.makeitvsolo.exchangeapi.service.impl;

import com.makeitvsolo.exchangeapi.datasource.CurrencyRepository;
import com.makeitvsolo.exchangeapi.datasource.ExchangeRepository;
import com.makeitvsolo.exchangeapi.domain.Exchange;
import com.makeitvsolo.exchangeapi.domain.mapping.MappedFromExchange;
import com.makeitvsolo.exchangeapi.service.ExchangeService;
import com.makeitvsolo.exchangeapi.service.dto.exchange.*;
import com.makeitvsolo.exchangeapi.service.exception.currency.CurrencyNotFoundException;
import com.makeitvsolo.exchangeapi.service.exception.exchange.ExchangeAlreadyExistsException;
import com.makeitvsolo.exchangeapi.service.exception.exchange.ExchangeNotFoundException;

public final class BaseExchangeService implements ExchangeService {
    private final ExchangeRepository exchangeRepository;
    private final CurrencyRepository currencyRepository;
    private final MappedFromExchange<ExchangeDto> mapper;

    public BaseExchangeService(
            ExchangeRepository exchangeRepository,
            CurrencyRepository currencyRepository,
            MappedFromExchange<ExchangeDto> mapper
    ) {
        this.exchangeRepository = exchangeRepository;
        this.currencyRepository = currencyRepository;
        this.mapper = mapper;
    }

    public ExchangeDto create(CreateExchangeDto payload) {
        if (exchangeRepository.fetchByCode(payload.base(), payload.target()).isPresent()) {
            throw new ExchangeAlreadyExistsException(payload.base(), payload.target());
        }

        var exchange = Exchange.create(
                currencyRepository.fetchByCode(payload.base())
                        .orElseThrow(() -> new CurrencyNotFoundException(payload.base())),

                currencyRepository.fetchByCode(payload.target())
                        .orElseThrow(() -> new CurrencyNotFoundException(payload.target())),

                payload.rate()
        );

        exchangeRepository.save(exchange);

        return exchange.map(mapper);
    }

    public ExchangeDto update(UpdateExchangeDto payload) {
        var exchange = exchangeRepository.fetchByCode(payload.base(), payload.target())
                               .orElseThrow(() -> new ExchangeNotFoundException(payload.base(), payload.target()));

        var updatedExchange = exchange.updated(payload.rate());

        exchangeRepository.update(updatedExchange);

        return updatedExchange.map(mapper);
    }

    public ExchangeDto byCode(ExchangeCodeDto code) {
        return exchangeRepository.fetchByCode(code.base(), code.target())
                       .map(exchange -> exchange.map(mapper))
                       .orElseThrow(() -> new ExchangeNotFoundException(code.base(), code.target()));
    }

    public ExchangeListDto all() {
        var exchanges = exchangeRepository.fetchAll()
                                .stream()
                                .map(exchange -> exchange.map(mapper))
                                .toList();

        return new ExchangeListDto(exchanges);
    }
}
