package com.sanalpos.bank.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanalpos.bank.domain.IdempotencyKey;
import com.sanalpos.bank.repository.IdempotencyKeyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class IdempotencyService {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyService.class);

    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final ObjectMapper objectMapper;

    public IdempotencyService(IdempotencyKeyRepository idempotencyKeyRepository,
                              ObjectMapper objectMapper) {
        this.idempotencyKeyRepository = idempotencyKeyRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public <T> Optional<CachedResponse<T>> findCached(String key, Class<T> responseType) {
        return idempotencyKeyRepository.findByKey(key)
                .map(entity -> {
                    try {
                        T body = objectMapper.readValue(entity.getResponseBody(), responseType);
                        return new CachedResponse<>(entity.getResponseStatus(), body);
                    } catch (JsonProcessingException e) {
                        log.warn("Idempotency kayıtlı cevap deserialize edilemedi: key={}", key, e);
                        return null;
                    }
                });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(String key, String endpoint, int status, Object body) {
        try {
            String json = objectMapper.writeValueAsString(body);
            IdempotencyKey entity = new IdempotencyKey();
            entity.setKey(key);
            entity.setEndpoint(endpoint);
            entity.setResponseStatus(status);
            entity.setResponseBody(json);
            idempotencyKeyRepository.save(entity);
        } catch (JsonProcessingException e) {
            log.error("Idempotency cevabı serialize edilemedi: key={}", key, e);
        }
    }

    public record CachedResponse<T>(int status, T body) {
    }
}