package com.commerce.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.commerce.project.model.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

}