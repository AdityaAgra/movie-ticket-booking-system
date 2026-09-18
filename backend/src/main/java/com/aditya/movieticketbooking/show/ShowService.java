package com.aditya.movieticketbooking.show;

import com.aditya.movieticketbooking.common.exception.ResourceNotFoundException;
import com.aditya.movieticketbooking.movie.Movie;
import com.aditya.movieticketbooking.movie.MovieRepository;
import com.aditya.movieticketbooking.show.dto.CreateShowRequest;
import com.aditya.movieticketbooking.show.dto.ShowResponse;
import com.aditya.movieticketbooking.show.dto.ShowSeatResponse;
import com.aditya.movieticketbooking.show.dto.ShowSummaryResponse;
import com.aditya.movieticketbooking.theater.Auditorium;
import com.aditya.movieticketbooking.theater.AuditoriumRepository;
import com.aditya.movieticketbooking.theater.Seat;
import com.aditya.movieticketbooking.theater.SeatRepository;
import java.util.List;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShowService {
    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final MovieRepository movieRepository;
    private final AuditoriumRepository auditoriumRepository;
    private final SeatRepository seatRepository;

    public ShowService(
            ShowRepository showRepository,
            ShowSeatRepository showSeatRepository,
            MovieRepository movieRepository,
            AuditoriumRepository auditoriumRepository,
            SeatRepository seatRepository) {
        this.showRepository = showRepository;
        this.showSeatRepository = showSeatRepository;
        this.movieRepository = movieRepository;
        this.auditoriumRepository = auditoriumRepository;
        this.seatRepository = seatRepository;
    }

    @Transactional
    public ShowResponse create(CreateShowRequest request) {
        Movie movie = movieRepository.findById(request.movieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found."));
        Auditorium auditorium = auditoriumRepository.findById(request.auditoriumId())
                .orElseThrow(() -> new ResourceNotFoundException("Auditorium not found."));

        Show show = showRepository.save(Show.create(movie, auditorium, request.startTime(), request.basePrice()));
        List<Seat> seats = seatRepository.findByAuditoriumId(auditorium.getId());
        List<ShowSeat> showSeats = seats.stream().map(seat -> ShowSeat.available(show, seat)).toList();
        showSeatRepository.saveAll(showSeats);

        return ShowResponse.from(show, showSeats.size());
    }

    @Transactional(readOnly = true)
    public List<ShowSummaryResponse> findByCityAndDate(java.util.UUID cityId, LocalDate date) {
        Instant startTime = date.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant endTime = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        return showRepository.findByCityAndStartTimeRange(cityId, startTime, endTime)
                .stream()
                .map(ShowSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ShowSeatResponse> findSeatMap(java.util.UUID showId) {
        if (!showRepository.existsById(showId)) {
            throw new ResourceNotFoundException("Show not found.");
        }
        return showSeatRepository.findSeatMapByShowId(showId).stream()
                .map(ShowSeatResponse::from)
                .toList();
    }
}
