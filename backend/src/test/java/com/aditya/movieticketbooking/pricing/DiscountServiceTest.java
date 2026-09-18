package com.aditya.movieticketbooking.pricing;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.aditya.movieticketbooking.common.enums.DiscountType;
import com.aditya.movieticketbooking.common.exception.InvalidRequestException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DiscountServiceTest {
    @Mock private DiscountCodeRepository discountCodeRepository;

    @Test
    void expiredDiscountIsRejected() {
        DiscountCode expired = DiscountCode.create(
                "EXPIRED", DiscountType.FIXED_AMOUNT, new BigDecimal("20.00"),
                Instant.now().minusSeconds(7_200), Instant.now().minusSeconds(3_600), true);
        when(discountCodeRepository.findByCodeIgnoreCase("EXPIRED")).thenReturn(Optional.of(expired));

        DiscountService discountService = new DiscountService(discountCodeRepository);
        assertThrows(InvalidRequestException.class, () -> discountService.findValidCode("EXPIRED", Instant.now()));
    }
}
