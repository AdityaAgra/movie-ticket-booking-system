package com.aditya.movieticketbooking.refund;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundPolicyRepository extends JpaRepository<RefundPolicy, UUID> {
    boolean existsByMinimumHoursBeforeShow(int minimumHoursBeforeShow);
    List<RefundPolicy> findByActiveTrueOrderByMinimumHoursBeforeShowDesc();
}
