package com.sanalpos.bank.service;

import com.sanalpos.bank.domain.Account;
import com.sanalpos.bank.domain.Customer;
import com.sanalpos.bank.dto.AccountRequest;
import com.sanalpos.bank.dto.AccountResponse;
import com.sanalpos.bank.exception.DuplicateResourceException;
import com.sanalpos.bank.exception.ResourceNotFoundException;
import com.sanalpos.bank.mapper.BankMapper;
import com.sanalpos.bank.repository.AccountRepository;
import com.sanalpos.bank.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final BankMapper bankMapper;

    public AccountService(AccountRepository accountRepository,
                          CustomerRepository customerRepository,
                          BankMapper bankMapper) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.bankMapper = bankMapper;
    }

    @Transactional
    public AccountResponse create(AccountRequest request) {
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer", "id", request.customerId()));

        if (accountRepository.existsByIban(request.iban())) {
            throw new DuplicateResourceException("Account", "iban", request.iban());
        }

        Account account = new Account();
        account.setCustomer(customer);
        account.setIban(request.iban());
        account.setCurrency(request.currency());
        account.setAccountType(request.accountType());
        account.setBalance(BigDecimal.ZERO);   // yeni hesap sıfır bakiyeyle açılır

        Account saved = accountRepository.save(account);
        return bankMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public AccountResponse getById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));
        return bankMapper.toResponse(account);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getByCustomerId(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer", "id", customerId);
        }
        return accountRepository.findByCustomerId(customerId)
                .stream()
                .map(bankMapper::toResponse)
                .toList();
    }
}