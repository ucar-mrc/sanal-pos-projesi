package com.sanalpos.bank.service;

import com.sanalpos.bank.domain.Customer;
import com.sanalpos.bank.dto.AuthResponse;
import com.sanalpos.bank.dto.LoginRequest;
import com.sanalpos.bank.dto.RegisterRequest;
import com.sanalpos.bank.exception.DuplicateResourceException;
import com.sanalpos.bank.repository.CustomerRepository;
import com.sanalpos.bank.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthService(CustomerRepository customerRepository, PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil, AuthenticationManager authenticationManager) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (customerRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Customer", "email", request.email());
        }

        Customer customer = new Customer();
        customer.setTckn(request.tckn());
        customer.setFirstName(request.firstName());
        customer.setLastName(request.lastName());
        customer.setEmail(request.email());
        customer.setPassword(passwordEncoder.encode(request.password())); // Şifreyi BCrypt ile hash'le

        Customer saved = customerRepository.save(customer);
        String token = jwtUtil.generateToken(saved.getEmail(), saved.getId());

        return new AuthResponse(token, saved.getEmail(), saved.getId());
    }

    public AuthResponse login(LoginRequest request) {
        // Spring Security'nin AuthenticationManager'ı kullanarak şifreyi kontrol et
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        Customer customer = customerRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalStateException("Kullanıcı bulunamadı"));

        String token = jwtUtil.generateToken(customer.getEmail(), customer.getId());
        return new AuthResponse(token, customer.getEmail(), customer.getId());
    }
}