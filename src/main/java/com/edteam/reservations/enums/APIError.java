package com.edteam.reservations.enums;

import org.springframework.http.HttpStatus;

public enum APIError {
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "There are attributes with wrong values"),
    BAD_FORMAT(HttpStatus.BAD_REQUEST, "The message does not have a correct form"),
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "Reservation not found"),
    RESERVATION_WITH_SAME_ID(HttpStatus.BAD_REQUEST, "There is a reservation with the same id"),
    CITY_NOT_FOUND(HttpStatus.NOT_FOUND, "City origin or destination not found"),
    EXCEED_NUMBER_OPERATIONS(HttpStatus.TOO_MANY_REQUESTS, "You exceed the number of operations"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid username or password"),;

    private final HttpStatus httpStatus;
    private final String message;

    APIError(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getMessage() {
        return message;
    }
}
