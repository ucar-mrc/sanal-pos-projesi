package com.sanalpos.bank.service;

import com.sanalpos.bank.domain.Customer;
import com.sanalpos.bank.dto.CustomerRequest;
import com.sanalpos.bank.dto.CustomerResponse;
import com.sanalpos.bank.exception.DuplicateResourceException;
import com.sanalpos.bank.exception.ResourceNotFoundException;
import com.sanalpos.bank.mapper.BankMapper;
import com.sanalpos.bank.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final BankMapper bankMapper;

    public CustomerService(CustomerRepository customerRepository, BankMapper bankMapper) {
        this.customerRepository = customerRepository;
        this.bankMapper = bankMapper;
    }

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        if (customerRepository.existsByTckn(request.tckn())) {
            throw new DuplicateResourceException("Customer", "tckn", request.tckn());
        }
        if (customerRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Customer", "email", request.email());
        }

        Customer customer = bankMapper.toEntity(request);
        Customer saved = customerRepository.save(customer);
        return bankMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> getAll() {
        return customerRepository.findAll()
                .stream()
                .map(bankMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponse getById(Long id) {
        Customer customer = findCustomerOrThrow(id);
        return bankMapper.toResponse(customer);
    }

    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = findCustomerOrThrow(id);

        if (!customer.getTckn().equals(request.tckn())
                && customerRepository.existsByTckn(request.tckn())) {
            throw new DuplicateResourceException("Customer", "tckn", request.tckn());
        }
        if (!customer.getEmail().equals(request.email())
                && customerRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Customer", "email", request.email());
        }

        customer.setTckn(request.tckn());
        customer.setFirstName(request.firstName());
        customer.setLastName(request.lastName());
        customer.setEmail(request.email());
        customer.setPhone(request.phone());

        // saveAndFlush: Hibernate'i hemen flush etmeye zorlar, böylece @PreUpdate çalışır.
        Customer saved = customerRepository.saveAndFlush(customer);
        return bankMapper.toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        Customer customer = findCustomerOrThrow(id);
        customerRepository.delete(customer);
    }

    private Customer findCustomerOrThrow(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
    }
}