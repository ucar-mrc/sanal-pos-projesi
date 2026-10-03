package com.sanalpos.bank.mapper;

import com.sanalpos.bank.domain.Account;
import com.sanalpos.bank.domain.Customer;
import com.sanalpos.bank.dto.AccountResponse;
import com.sanalpos.bank.dto.CustomerRequest;
import com.sanalpos.bank.dto.CustomerResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BankMapper {

    CustomerResponse toResponse(Customer customer);

    @Mapping(source = "customer.id", target = "customerId")
    AccountResponse toResponse(Account account);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "accounts", ignore = true)
    @Mapping(target = "cards", ignore = true)
    Customer toEntity(CustomerRequest request);
}