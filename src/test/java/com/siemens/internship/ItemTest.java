package com.siemens.internship;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ItemTest {
    private Validator validator;

    @BeforeEach
    public void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testValidItemCreation() {
        Item item = new Item(1L, "Test Item", "A test item description", "Available", "test@example.com");
        assertThat(validator.validate(item)).isEmpty();
    }

    @Test
    public void testInvalidEmail() {
        Item item = new Item(2L, "Invalid Email Item", "Description", "Available", "invalid-email");
        var violations = validator.validate(item);
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Please provide a valid email address");
    }


    @Test
    public void testNullEmail() {
        Item item = new Item(4L, "Null Email Item", "Description", "Available", null);
        var violations = validator.validate(item);
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Email cannot be empty");
    }
}