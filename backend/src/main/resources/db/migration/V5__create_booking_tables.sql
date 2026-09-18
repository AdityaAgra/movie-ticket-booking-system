CREATE TABLE bookings (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    show_id UUID NOT NULL REFERENCES shows(id),
    status VARCHAR(20) NOT NULL,
    total_amount NUMERIC(10, 2) NOT NULL CHECK (total_amount >= 0),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_booking_status CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'EXPIRED'))
);

CREATE TABLE booking_seats (
    id UUID PRIMARY KEY,
    booking_id UUID NOT NULL REFERENCES bookings(id),
    show_seat_id UUID NOT NULL REFERENCES show_seats(id),
    price_at_booking NUMERIC(10, 2) NOT NULL CHECK (price_at_booking >= 0),
    CONSTRAINT uq_booking_show_seat UNIQUE (booking_id, show_seat_id)
);

CREATE INDEX idx_bookings_user_created ON bookings (user_id, created_at DESC);
