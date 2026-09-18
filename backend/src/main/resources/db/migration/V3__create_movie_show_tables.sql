CREATE TABLE movies (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    duration_minutes INTEGER NOT NULL CHECK (duration_minutes > 0)
);

CREATE TABLE shows (
    id UUID PRIMARY KEY,
    movie_id UUID NOT NULL REFERENCES movies(id),
    auditorium_id UUID NOT NULL REFERENCES auditoriums(id),
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    base_price NUMERIC(10, 2) NOT NULL CHECK (base_price >= 0)
);

CREATE TABLE show_seats (
    id UUID PRIMARY KEY,
    show_id UUID NOT NULL REFERENCES shows(id),
    seat_id UUID NOT NULL REFERENCES seats(id),
    status VARCHAR(20) NOT NULL,
    held_by_user_id UUID NULL REFERENCES users(id),
    hold_expiry TIMESTAMP WITH TIME ZONE NULL,
    CONSTRAINT uq_show_seat UNIQUE (show_id, seat_id),
    CONSTRAINT chk_show_seat_status CHECK (status IN ('AVAILABLE', 'HELD', 'BOOKED'))
);

CREATE INDEX idx_show_seats_hold_expiry ON show_seats (status, hold_expiry);
