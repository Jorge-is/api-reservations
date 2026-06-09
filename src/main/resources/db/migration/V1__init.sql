CREATE TABLE price (
    id          BIGSERIAL PRIMARY KEY,
    total_price NUMERIC(19, 2),
    total_tax   NUMERIC(19, 2),
    base_price  NUMERIC(19, 2)
);

CREATE TABLE itinerary (
    id       BIGSERIAL PRIMARY KEY,
    price_id BIGINT REFERENCES price (id)
);

CREATE TABLE reservation (
    id           BIGSERIAL PRIMARY KEY,
    itinerary_id BIGINT REFERENCES itinerary (id)
);

CREATE TABLE passenger (
    id              BIGSERIAL PRIMARY KEY,
    first_name      VARCHAR(255),
    last_name       VARCHAR(255),
    document_number VARCHAR(100),
    document_type   VARCHAR(50),
    birthday        DATE,
    reservation_id  BIGINT REFERENCES reservation (id)
);

CREATE TABLE segment (
    id           BIGSERIAL PRIMARY KEY,
    origin       VARCHAR(3)   NOT NULL,
    destination  VARCHAR(3)   NOT NULL,
    departure    VARCHAR(255),
    arrival      VARCHAR(255),
    carrier      VARCHAR(255),
    itinerary_id BIGINT REFERENCES itinerary (id)
);
