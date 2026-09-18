CREATE TABLE discount_codes (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    discount_type VARCHAR(20) NOT NULL,
    value NUMERIC(10, 2) NOT NULL CHECK (value > 0),
    valid_from TIMESTAMP WITH TIME ZONE NOT NULL,
    valid_to TIMESTAMP WITH TIME ZONE NOT NULL,
    active BOOLEAN NOT NULL,
    CONSTRAINT chk_discount_type CHECK (discount_type IN ('PERCENTAGE', 'FIXED_AMOUNT')),
    CONSTRAINT chk_discount_date_range CHECK (valid_to >= valid_from)
);

CREATE TABLE refund_policies (
    id UUID PRIMARY KEY,
    minimum_hours_before_show INTEGER NOT NULL UNIQUE CHECK (minimum_hours_before_show >= 0),
    refund_percentage NUMERIC(5, 2) NOT NULL CHECK (refund_percentage >= 0 AND refund_percentage <= 100),
    active BOOLEAN NOT NULL
);
