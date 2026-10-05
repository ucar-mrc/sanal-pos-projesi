package com.sanalpos.bank.controller;

import com.sanalpos.bank.dto.CardRequest;
import com.sanalpos.bank.dto.CardResponse;
import com.sanalpos.bank.service.CardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping
    public ResponseEntity<CardResponse> create(@Valid @RequestBody CardRequest request) {
        CardResponse response = cardService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.getById(id));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<CardResponse>> getByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok(cardService.getByCustomerId(customerId));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        cardService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}