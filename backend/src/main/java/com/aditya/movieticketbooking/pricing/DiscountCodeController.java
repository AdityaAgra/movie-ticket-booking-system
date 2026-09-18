package com.aditya.movieticketbooking.pricing;

import com.aditya.movieticketbooking.pricing.dto.CreateDiscountCodeRequest;
import com.aditya.movieticketbooking.pricing.dto.DiscountCodeResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/discount-codes")
public class DiscountCodeController {
    private final DiscountService discountService;
    public DiscountCodeController(DiscountService discountService) { this.discountService = discountService; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DiscountCodeResponse create(@Valid @RequestBody CreateDiscountCodeRequest request) {
        return discountService.create(request);
    }
}
