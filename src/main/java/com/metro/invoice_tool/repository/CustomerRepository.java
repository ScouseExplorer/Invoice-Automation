package com.metro.invoice_tool.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.metro.invoice_tool.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);
}
