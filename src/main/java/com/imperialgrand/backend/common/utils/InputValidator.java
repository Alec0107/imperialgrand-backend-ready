package com.imperialgrand.backend.common.utils;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class InputValidator {

    private static String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    private static String PASS_REGEX = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[\\d]).{8,}$";
    private static String NAME_REGEX = "^[A-Za-z]+(?:[' -][A-Za-z]+)*$";

    public static void validateName(String name, String fieldName) {
        if (name == null || !name.matches(NAME_REGEX)) {
            throw new IllegalArgumentException(fieldName + " must contain only letters, and may include hyphens or apostrophes.");
        }
    }

    public static void validateEmail(String email) {
        if(email == null || !email.matches(EMAIL_REGEX)) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    public static void validatePassword(String password) {
        if(password == null || !password.matches(PASS_REGEX)) {
            throw new IllegalArgumentException("Password must have at least 8 characters, 1 lowercase, 1 uppercase, and 1 digit");
        }
    }

    public static void validatePhoneNumber(String username) {}

    public static void runValidation(Consumer<String> validatorFunction, String input){
        validatorFunction.accept(input);
    }

    public static void runValidation(BiConsumer<String, String> validatorFunction, String input, String fieldName){
        validatorFunction.accept(input, fieldName);
    }


}
