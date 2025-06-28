package dev.modig.insurance.exception;

public class InsuranceNotFoundException extends RuntimeException {
    public InsuranceNotFoundException(String personalNumber) {
        super("Insurance not found: " + personalNumber);
    }
}
