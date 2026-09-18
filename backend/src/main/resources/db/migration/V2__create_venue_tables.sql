CREATE TABLE cities (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE theaters (
    id UUID PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    address VARCHAR(255) NOT NULL,
    city_id UUID NOT NULL REFERENCES cities(id)
);

CREATE TABLE auditoriums (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    theater_id UUID NOT NULL REFERENCES theaters(id)
);

CREATE TABLE seats (
    id UUID PRIMARY KEY,
    row_label VARCHAR(10) NOT NULL,
    seat_number INTEGER NOT NULL CHECK (seat_number > 0),
    seat_type VARCHAR(20) NOT NULL,
    auditorium_id UUID NOT NULL REFERENCES auditoriums(id),
    CONSTRAINT uq_seat_position UNIQUE (auditorium_id, row_label, seat_number),
    CONSTRAINT chk_seat_type CHECK (seat_type IN ('REGULAR', 'PREMIUM'))
);
