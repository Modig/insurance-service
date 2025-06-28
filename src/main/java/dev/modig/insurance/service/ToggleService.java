package dev.modig.insurance.service;

import org.ff4j.FF4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ToggleService {

    private static final List<String> TOGGLED_USERS = List.of("199001011234", "190101010023");
    private final FF4j ff4j;

    public ToggleService(FF4j ff4j) {
        this.ff4j = ff4j;
    }

    /**
     * Checks whether a discount is enabled for a given personal number.
     * Discount is enabled if the user is in a 20% canary group or explicitly toggled while the DISCOUNT_CAMPAIGN flag is active.
     */
    public boolean isDiscountEnabledFor(String personalNumber) {
        return isInCanaryGroup(personalNumber) || (ff4j.check("DISCOUNT_CAMPAIGN") && TOGGLED_USERS.contains(personalNumber));
    }

    /**
     * Determines if a user is part of the canary rollout group (20% hash-based).
     */
    boolean isInCanaryGroup(String personalNumber) {
        return Math.abs(personalNumber.hashCode() % 100) < 20;
    }
}

