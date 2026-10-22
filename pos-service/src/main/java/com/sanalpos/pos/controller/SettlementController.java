package com.sanalpos.pos.controller;

import com.sanalpos.pos.dto.SettlementRequest;
import com.sanalpos.pos.dto.SettlementResponse;
import com.sanalpos.pos.service.SettlementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/settlements")
public class SettlementController {

    private static final String IDEMPOTENCY_HEADER = "Idempotency-Key";

    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @PostMapping
    public ResponseEntity<SettlementResponse> requestSettlement(
            @RequestHeader(value = IDEMPOTENCY_HEADER, required = false) String idempotencyKey,
            @Valid @RequestBody SettlementRequest request) {

        SettlementResponse response = settlementService.requestSettlement(request, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}