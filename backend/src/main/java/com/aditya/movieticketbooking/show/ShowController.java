package com.aditya.movieticketbooking.show;

import com.aditya.movieticketbooking.show.dto.CreateShowRequest;
import com.aditya.movieticketbooking.show.dto.ShowResponse;
import com.aditya.movieticketbooking.show.dto.ShowSeatResponse;
import com.aditya.movieticketbooking.show.dto.ShowSummaryResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/shows")
public class ShowController {
    private final ShowService showService;
    public ShowController(ShowService showService) { this.showService = showService; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShowResponse create(@Valid @RequestBody CreateShowRequest request) {
        return showService.create(request);
    }

    @GetMapping
    public List<ShowSummaryResponse> findByCityAndDate(
            @RequestParam @NotNull UUID cityId,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return showService.findByCityAndDate(cityId, date);
    }

    @GetMapping("/{showId}/seats")
    public List<ShowSeatResponse> findSeatMap(@PathVariable UUID showId) {
        return showService.findSeatMap(showId);
    }
}
