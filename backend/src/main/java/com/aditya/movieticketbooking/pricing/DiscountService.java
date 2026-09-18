package com.aditya.movieticketbooking.pricing;

import com.aditya.movieticketbooking.common.exception.BusinessConflictException;
import com.aditya.movieticketbooking.common.exception.InvalidRequestException;
import com.aditya.movieticketbooking.pricing.dto.CreateDiscountCodeRequest;
import com.aditya.movieticketbooking.pricing.dto.DiscountCodeResponse;
import java.time.Instant;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DiscountService {
    private final DiscountCodeRepository discountCodeRepository;
    public DiscountService(DiscountCodeRepository discountCodeRepository) { this.discountCodeRepository = discountCodeRepository; }

    @Transactional
    public DiscountCodeResponse create(CreateDiscountCodeRequest request) {
        if (request.validTo().isBefore(request.validFrom())) {
            throw new InvalidRequestException("Discount end time must be after its start time.");
        }
        String code = request.code().trim().toUpperCase(Locale.ROOT);
        if (discountCodeRepository.existsByCodeIgnoreCase(code)) {
            throw new BusinessConflictException("A discount code with this value already exists.");
        }
        return DiscountCodeResponse.from(discountCodeRepository.save(DiscountCode.create(
                code, request.discountType(), request.value(), request.validFrom(), request.validTo(), request.active())));
    }

    @Transactional(readOnly = true)
    public DiscountCode findValidCode(String code, Instant appliedAt) {
        DiscountCode discountCode = discountCodeRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new InvalidRequestException("Discount code is invalid."));
        if (!discountCode.isActive() || appliedAt.isBefore(discountCode.getValidFrom()) || appliedAt.isAfter(discountCode.getValidTo())) {
            throw new InvalidRequestException("Discount code is not currently valid.");
        }
        return discountCode;
    }
}
