package com.sanalpos.bank.service;

import com.sanalpos.bank.domain.*;
import com.sanalpos.bank.dto.*;
import com.sanalpos.bank.exception.InsufficientBalanceException;
import com.sanalpos.bank.exception.ResourceNotFoundException;
import com.sanalpos.bank.repository.AccountRepository;
import com.sanalpos.bank.repository.CardRepository;
import com.sanalpos.bank.repository.TransactionRepository;
import com.sanalpos.bank.util.CardHashUtil;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;

    public TransactionService(AccountRepository accountRepository,
                              TransactionRepository transactionRepository,
                              CardRepository cardRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.cardRepository = cardRepository;
    }

    // =====================================================
    // DEBIT — hesaptan para çek
    // =====================================================
    @Transactional
    public TransactionResponse debit(DebitRequest request) {
        Account account = findAccountWithLockOrThrow(request.accountId());
        BigDecimal amount = request.amount();

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(account.getBalance(), amount);
        }

        account.setBalance(account.getBalance().subtract(amount));

        Transaction tx = buildTransaction(
                account,
                TransactionType.DEBIT,
                amount,
                account.getBalance(),
                null,
                request.description()
        );
        transactionRepository.save(tx);

        return toResponse(tx);
    }

    // =====================================================
    // CREDIT — hesaba para yatır
    // =====================================================
    @Transactional
    public TransactionResponse credit(CreditRequest request) {
        Account account = findAccountWithLockOrThrow(request.accountId());
        BigDecimal amount = request.amount();

        account.setBalance(account.getBalance().add(amount));

        Transaction tx = buildTransaction(
                account,
                TransactionType.CREDIT,
                amount,
                account.getBalance(),
                null,
                request.description()
        );
        transactionRepository.save(tx);

        return toResponse(tx);
    }

    // =====================================================
    // TRANSFER — iki hesap arası atomik transfer
    // =====================================================
    @Transactional
    public TransactionResponse transfer(TransferRequest request) {
        if (request.fromAccountId().equals(request.toAccountId())) {
            throw new IllegalArgumentException("Aynı hesaba transfer yapılamaz");
        }

        // DEADLOCK ÖNLEME: Her zaman küçük ID'li hesabı önce kilitle
        Long firstId = Math.min(request.fromAccountId(), request.toAccountId());
        Long secondId = Math.max(request.fromAccountId(), request.toAccountId());

        Account first = findAccountWithLockOrThrow(firstId);
        Account second = findAccountWithLockOrThrow(secondId);

        Account fromAccount = first.getId().equals(request.fromAccountId()) ? first : second;
        Account toAccount   = first.getId().equals(request.fromAccountId()) ? second : first;

        BigDecimal amount = request.amount();

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException(
                    "Yetersiz bakiye. Mevcut: " + fromAccount.getBalance() + ", istenen: " + amount);
        }

        // Ortak referans ID — iki bacağı eşleştirir
        String referenceId = UUID.randomUUID().toString();

        // 1. Gönderen hesaptan düş
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        Transaction outTx = buildTransaction(
                fromAccount,
                TransactionType.TRANSFER_OUT,
                amount,
                fromAccount.getBalance(),
                referenceId,
                request.description()
        );
        transactionRepository.save(outTx);

        // 2. Alan hesaba ekle
        toAccount.setBalance(toAccount.getBalance().add(amount));
        Transaction inTx = buildTransaction(
                toAccount,
                TransactionType.TRANSFER_IN,
                amount,
                toAccount.getBalance(),
                referenceId,
                request.description()
        );
        transactionRepository.save(inTx);

        // Response olarak gönderen tarafın bacağını dön (client'ın baktığı taraf)
        return toResponse(outTx);
    }

    // =====================================================
    // GET — Hesap hareketleri
    // =====================================================
    @Transactional(readOnly = true)
    public List<TransactionResponse> getAccountTransactions(Long accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new ResourceNotFoundException("Account", "id", accountId);
        }
        return transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =====================================================
    // YARDIMCI METOTLAR
    // =====================================================
    private Account findAccountWithLockOrThrow(Long id) {
        return accountRepository.findByIdWithLock(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));
    }

    private Transaction buildTransaction(Account account,
                                         TransactionType type,
                                         BigDecimal amount,
                                         BigDecimal balanceAfter,
                                         String referenceId,
                                         String description) {
        Transaction tx = new Transaction();
        tx.setAccount(account);
        tx.setType(type);
        tx.setAmount(amount);
        tx.setCurrency(account.getCurrency());
        tx.setBalanceAfter(balanceAfter);
        tx.setReferenceId(referenceId);
        tx.setDescription(description);
        return tx;
    }

    private TransactionResponse toResponse(Transaction tx) {
        return new TransactionResponse(
                tx.getId(),
                tx.getAccount().getId(),
                tx.getType(),
                tx.getAmount(),
                tx.getCurrency(),
                tx.getBalanceAfter(),
                tx.getReferenceId(),
                tx.getDescription(),
                tx.getCreatedAt()
        );
    }

    @Transactional
    public CardPaymentResponse cardPayment(CardPaymentRequest request) {
        String normalized = CardHashUtil.normalize(request.cardNumber());
        String hash = CardHashUtil.sha256(normalized);

        Card card = cardRepository.findByCardNumberHash(hash)
                .orElseThrow(() -> new ResourceNotFoundException("Card", "cardNumber",
                        "****" + CardHashUtil.last4(normalized)));

        if (!card.isActive()) {
            throw new IllegalArgumentException("Kart pasif durumda");
        }

        Customer customer = card.getCustomer();

        // Müşterinin birincil TRY/CHECKING hesabını bul
        Account account = accountRepository.findByCustomerId(customer.getId())
                .stream()
                .filter(a -> a.getCurrency() == Currency.TRY
                        && a.getAccountType() == AccountType.CHECKING)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account", "customerId", customer.getId()));

        // Lock + bakiye kontrolü + debit
        Account locked = accountRepository.findByIdWithLock(account.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", account.getId()));

        if (locked.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientBalanceException(locked.getBalance(), request.amount());
        }

        locked.setBalance(locked.getBalance().subtract(request.amount()));

        String referenceId = UUID.randomUUID().toString();
        Transaction tx = buildTransaction(
                locked,
                TransactionType.DEBIT,
                request.amount(),
                locked.getBalance(),
                referenceId,
                request.description() != null ? request.description() : "Card payment"
        );
        transactionRepository.save(tx);

        return new CardPaymentResponse(
                tx.getId(),
                referenceId,
                tx.getAmount(),
                tx.getBalanceAfter(),
                CardHashUtil.last4(normalized),
                "AUTH_OK",
                tx.getCreatedAt()
        );
    }
}