package com.edteam.reservations.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CityFormatValidatorTest {

    private final CityFormatValidator validator = new CityFormatValidator();

    @Test
    void valid_threeUppercaseLetters_returnsTrue() {
        assertTrue(validator.isValid("MAD", null));
        assertTrue(validator.isValid("BCN", null));
        assertTrue(validator.isValid("EZE", null));
    }

    @Test
    void nullValue_returnsFalse() {
        assertFalse(validator.isValid(null, null));
    }

    @Test
    void lowercase_returnsFalse() {
        assertFalse(validator.isValid("mad", null));
    }

    @Test
    void mixedCase_returnsFalse() {
        assertFalse(validator.isValid("Mad", null));
    }

    @Test
    void tooShort_returnsFalse() {
        assertFalse(validator.isValid("MA", null));
    }

    @Test
    void tooLong_returnsFalse() {
        assertFalse(validator.isValid("MADR", null));
    }

    @Test
    void containsDigit_returnsFalse() {
        assertFalse(validator.isValid("M4D", null));
    }

    @Test
    void empty_returnsFalse() {
        assertFalse(validator.isValid("", null));
    }
}
