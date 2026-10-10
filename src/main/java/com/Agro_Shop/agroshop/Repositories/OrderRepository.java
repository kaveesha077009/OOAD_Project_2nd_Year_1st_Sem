package com.Agro_Shop.agroshop.Repositories;

import com.Agro_Shop.agroshop.Entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
}