package dev.modig.insurance.controller;

import dev.modig.insurance.dto.PersonInsuranceResponse;
import dev.modig.insurance.exception.InsuranceNotFoundException;
import dev.modig.insurance.model.HealthInsurance;
import dev.modig.insurance.service.InsuranceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InsuranceControllerUnitTest {

    private InsuranceService insuranceService;
    private InsuranceController controller;

    @BeforeEach
    void setUp() {
        insuranceService = mock(InsuranceService.class);
        controller = new InsuranceController(insuranceService);
    }

    @Test
    void shouldReturnResponseFromServiceForValidPersonalNumber() {
        String input = "19900101-1234";
        String expected = "199001011234";

        PersonInsuranceResponse mockResponse = new PersonInsuranceResponse(expected, List.of(new HealthInsurance()), 500, null);
        when(insuranceService.getInsurancesFor(expected)).thenReturn(mockResponse);

        ResponseEntity<?> response = controller.getInsuranceInfo(input);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(mockResponse, response.getBody());
    }

    @Test
    void shouldStripHyphenFromPersonalNumber() {
        String input = "20001212-5678";
        String expected = "200012125678";

        when(insuranceService.getInsurancesFor(expected)).thenReturn(mock(PersonInsuranceResponse.class));

        controller.getInsuranceInfo(input);

        verify(insuranceService).getInsurancesFor(expected);
    }

    @Test
    void shouldReturnBadRequestForInvalidFormat() {
        String input = "ABC123";

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> controller.getInsuranceInfo(input));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Invalid personal number", ex.getReason());
    }

    @Test
    void shouldReturn404WhenInsuranceNotFound() {
        String input = "19900101-0000";
        String stripped = "199001010000";

        when(insuranceService.getInsurancesFor(stripped)).thenThrow(new InsuranceNotFoundException(stripped));

        ResponseEntity<?> response = controller.getInsuranceInfo(input);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Insurance not found", response.getBody());
    }
}
