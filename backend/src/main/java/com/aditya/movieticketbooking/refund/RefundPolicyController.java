package com.aditya.movieticketbooking.refund;

import com.aditya.movieticketbooking.refund.dto.CreateRefundPolicyRequest;
import com.aditya.movieticketbooking.refund.dto.RefundPolicyResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/refund-policies")
public class RefundPolicyController {
    private final RefundService refundService;
    public RefundPolicyController(RefundService refundService) { this.refundService = refundService; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RefundPolicyResponse create(@Valid @RequestBody CreateRefundPolicyRequest request) {
        return refundService.create(request);
    }
}
