package com.commerce.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.commerce.project.model.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>{

}