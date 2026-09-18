package com.aditya.movieticketbooking.pricing;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscountCodeRepository extends JpaRepository<DiscountCode, UUID> {
    Optional<DiscountCode> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);
}
