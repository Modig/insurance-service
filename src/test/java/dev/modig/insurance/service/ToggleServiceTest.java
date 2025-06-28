package dev.modig.insurance.service;

import org.ff4j.FF4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ToggleServiceTest {

    private FF4j ff4j;
    private ToggleService toggleService;

    @BeforeEach
    void setUp() {
        ff4j = mock(FF4j.class);
        toggleService = new ToggleService(ff4j);
    }

    @Test
    void shouldReturnTrueForCanaryGroup() {
        String personalNumber = "199001011234";
        when(ff4j.check("DISCOUNT_CAMPAIGN")).thenReturn(true);

        boolean result = toggleService.isDiscountEnabledFor(personalNumber);

        assertTrue(result, "User in canary group should receive discount");
    }

    @Test
    void shouldReturnTrueForToggledUserWhenFlagEnabled() {
        String personalNumber = "190101010023"; // in TOGGLED_USERS
        when(ff4j.check("DISCOUNT_CAMPAIGN")).thenReturn(true);

        boolean result = toggleService.isDiscountEnabledFor(personalNumber);

        assertTrue(result);
    }

    @Test
    void shouldReturnFalseForNonToggledUserWhenFlagOff() {
        String personalNumber = "198512309999"; // not in TOGGLED_USERS
        when(ff4j.check("DISCOUNT_CAMPAIGN")).thenReturn(false);

        boolean result = toggleService.isDiscountEnabledFor(personalNumber);

        assertFalse(result);
    }

    @Test
    void shouldReturnFalseIfFlagEnabledButUserNotInList() {
        String personalNumber = "198001019999"; // not in TOGGLED_USERS, hash 82
        when(ff4j.check("DISCOUNT_CAMPAIGN")).thenReturn(true);

        boolean result = toggleService.isDiscountEnabledFor(personalNumber);

        assertFalse(result);
    }

    @Test
    void shouldReturnFalseForInvalidCanaryHash() {
        String personalNumber = "some-user-id"; // hash = 48

        boolean result = toggleService.isInCanaryGroup(personalNumber);

        assertFalse(result);
    }

    @Test
    void shouldReturnTrueForValidCanaryHash() {
        String personalNumber = "included-user"; // hash = 12

        boolean result = toggleService.isInCanaryGroup(personalNumber);

        assertTrue(result);
    }
}
