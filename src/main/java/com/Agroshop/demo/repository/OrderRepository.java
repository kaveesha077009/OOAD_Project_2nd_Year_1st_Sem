package com.agroshop.demo.repository;

import com.agroshop.demo.entity.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {
    List<CustomerOrder> findAllByOrderByIdDesc();
    List<CustomerOrder> findByCustomerIdOrderByIdDesc(Long customerId);
}
