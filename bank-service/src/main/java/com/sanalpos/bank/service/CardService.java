package com.sanalpos.bank.service;

import com.sanalpos.bank.domain.Card;
import com.sanalpos.bank.domain.Customer;
import com.sanalpos.bank.dto.CardRequest;
import com.sanalpos.bank.dto.CardResponse;
import com.sanalpos.bank.exception.DuplicateResourceException;
import com.sanalpos.bank.exception.ResourceNotFoundException;
import com.sanalpos.bank.repository.CardRepository;
import com.sanalpos.bank.repository.CustomerRepository;
import com.sanalpos.bank.util.CardHashUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final CustomerRepository customerRepository;

    public CardService(CardRepository cardRepository, CustomerRepository customerRepository) {
        this.cardRepository = cardRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public CardResponse create(CardRequest request) {
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer", "id", request.customerId()));

        String normalized = CardHashUtil.normalize(request.cardNumber());
        String hash = CardHashUtil.sha256(normalized);

        if (cardRepository.existsByCardNumberHash(hash)) {
            throw new DuplicateResourceException("Card", "cardNumber", "****"
                    + CardHashUtil.last4(normalized));
        }

        // CVV request'ten alındı ama HİÇBİR YERDE SAKLANMIYOR - PCI-DSS
        // Burada gerçekte kart bankasına gönderilir ve doğrulanır.
        // Simülasyonda sadece format kontrolü yeterli (DTO'daki @Pattern yapıyor).

        Card card = new Card();
        card.setCustomer(customer);
        card.setCardNumberHash(hash);
        card.setCardNumberLast4(CardHashUtil.last4(normalized));
        card.setCardHolderName(request.cardHolderName());
        card.setExpiryMonth(request.expiryMonth());
        card.setExpiryYear(request.expiryYear());
        card.setCardType(request.cardType());
        card.setActive(true);

        Card saved = cardRepository.save(card);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public CardResponse getById(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Card", "id", id));
        return toResponse(card);
    }

    @Transactional(readOnly = true)
    public List<CardResponse> getByCustomerId(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer", "id", customerId);
        }
        return cardRepository.findByCustomerId(customerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void deactivate(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Card", "id", id));
        card.setActive(false);
        // @Transactional + dirty checking sayesinde save() gerekmez
    }

    // =====================================================
    // YARDIMCI
    // =====================================================
    private CardResponse toResponse(Card card) {
        return new CardResponse(
                card.getId(),
                card.getCustomer().getId(),
                maskCardNumber(card.getCardNumberLast4()),
                card.getCardHolderName(),
                card.getExpiryMonth(),
                card.getExpiryYear(),
                card.getCardType(),
                card.isActive(),
                card.getCreatedAt(),
                card.getUpdatedAt()
        );
    }

    /**
     * Kart numarasını maskeler: "**** **** **** 1234"
     */
    private String maskCardNumber(String last4) {
        return "**** **** **** " + last4;
    }
}