package com.erick.order_api.repository;

import com.erick.order_api.entity.WholesaleOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WholesaleOrderRepository extends JpaRepository<WholesaleOrder, UUID> {
}
