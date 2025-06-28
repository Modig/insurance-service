package dev.modig.insurance.service;

import dev.modig.insurance.dto.PersonInsuranceResponse;
import dev.modig.insurance.exception.InsuranceNotFoundException;
import dev.modig.insurance.exception.VehicleNotFoundException;
import dev.modig.insurance.model.*;
import dev.modig.insurance.repository.InsuranceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class InsuranceServiceTest {

    private InsuranceRepository repository;
    private ToggleService toggleService;
    private WebClient vehicleWebClient;
    private InsuranceService insuranceService;

    @BeforeEach
    void setUp() {
        repository = mock(InsuranceRepository.class);
        toggleService = mock(ToggleService.class);
        vehicleWebClient = mock(WebClient.class, RETURNS_DEEP_STUBS);

        insuranceService = new InsuranceService(repository, vehicleWebClient, toggleService);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void shouldReturnResponseWithEnrichedCarInsuranceAndDiscount() {
        String personalNumber = "199001011234";
        CarInsurance insurance = CarInsurance.carInsurance("ABC123");
        Vehicle vehicle = new Vehicle("ABC123", "Volvo", "XC90", 2020);

        WebClient.RequestHeadersUriSpec uriSpecMock = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec headersSpecMock = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpecMock = mock(WebClient.ResponseSpec.class);

        when(vehicleWebClient.get()).thenReturn(uriSpecMock);
        when(uriSpecMock.uri(("/{registrationNumber}"), ("ABC123"))).thenReturn(headersSpecMock);
        when(headersSpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.onStatus(any(), any())).thenReturn(responseSpecMock);
        when(responseSpecMock.bodyToMono(Vehicle.class)).thenReturn(Mono.just(vehicle));
        when(repository.findByPersonalNumber(personalNumber)).thenReturn(Optional.of(List.of(insurance)));
        when(toggleService.isDiscountEnabledFor(personalNumber)).thenReturn(true);

        PersonInsuranceResponse response = insuranceService.getInsurancesFor(personalNumber);

        assertEquals(personalNumber, response.personalNumber());
        assertEquals(1, response.insurances().size());
        assertEquals(vehicle, ((CarInsurance) response.insurances().getFirst()).vehicle());
        assertEquals(InsuranceType.CAR.getMonthlyCost(), response.totalCost());
        assertEquals((int) Math.round(InsuranceType.CAR.getMonthlyCost() * 0.9), response.discountedTotalCost());
    }


    @Test
    void shouldReturnResponseWithUnenrichedCarInsuranceWhenVehicleNotFound() {
        String personalNumber = "199001011234";
        CarInsurance insurance = CarInsurance.carInsurance("ABC123");

        when(repository.findByPersonalNumber(personalNumber)).thenReturn(Optional.of(List.of(insurance)));
        when(vehicleWebClient.get()
                .uri("/ABC123", "ABC123")
                .retrieve()
                .onStatus(any(), any())
                .bodyToMono(Vehicle.class)
        ).thenReturn(Mono.error(new VehicleNotFoundException("Vehicle not found")));
        when(toggleService.isDiscountEnabledFor(personalNumber)).thenReturn(false);

        PersonInsuranceResponse response = insuranceService.getInsurancesFor(personalNumber);

        assertEquals(personalNumber, response.personalNumber());
        assertEquals(1, response.insurances().size());
        CarInsurance resultInsurance = (CarInsurance) response.insurances().getFirst();
        assertEquals("ABC123", resultInsurance.registrationNumber());
        assertNull(resultInsurance.vehicle());
        assertEquals(InsuranceType.CAR.getMonthlyCost(), response.totalCost());
        assertNull(response.discountedTotalCost());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void shouldReturnResponseWithMultipleInsurancesAndApplyDiscount() {
        String personalNumber = "199012319999";

        CarInsurance carInsurance = CarInsurance.carInsurance("CAR123");
        HealthInsurance healthInsurance = new HealthInsurance();
        PetInsurance petInsurance = new PetInsurance();
        Vehicle vehicle = new Vehicle("CAR123", "Tesla", "Model 3", 2023);

        WebClient.RequestHeadersUriSpec uriSpecMock = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec headersSpecMock = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpecMock = mock(WebClient.ResponseSpec.class);

        when(vehicleWebClient.get()).thenReturn(uriSpecMock);
        when(uriSpecMock.uri(("/{registrationNumber}"), ("CAR123"))).thenReturn(headersSpecMock);
        when(headersSpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.onStatus(any(), any())).thenReturn(responseSpecMock);
        when(responseSpecMock.bodyToMono(Vehicle.class)).thenReturn(Mono.just(vehicle));
        when(repository.findByPersonalNumber(personalNumber))
                .thenReturn(Optional.of(List.of(carInsurance, healthInsurance, petInsurance)));
        when(toggleService.isDiscountEnabledFor(personalNumber)).thenReturn(true);

        PersonInsuranceResponse response = insuranceService.getInsurancesFor(personalNumber);

        int expectedTotal = InsuranceType.CAR.getMonthlyCost()
                + InsuranceType.HEALTH.getMonthlyCost()
                + InsuranceType.PET.getMonthlyCost();

        int expectedDiscounted = (int) Math.round(expectedTotal * 0.9);
        CarInsurance resultCar = (CarInsurance) response.insurances().stream()
                .filter(CarInsurance.class::isInstance)
                .findFirst()
                .orElseThrow();

        assertEquals(3, response.insurances().size());
        assertEquals(expectedTotal, response.totalCost());
        assertEquals(expectedDiscounted, response.discountedTotalCost());
        assertEquals(vehicle, resultCar.vehicle());
    }

    @Test
    void shouldThrowInsuranceNotFoundExceptionWhenNoInsuranceExists() {
        String personalNumber = "no-insurance";
        when(repository.findByPersonalNumber(personalNumber)).thenReturn(Optional.empty());

        assertThrows(InsuranceNotFoundException.class, () -> insuranceService.getInsurancesFor(personalNumber));
    }

    @Test
    void shouldReturnResponseWithoutDiscountWhenNotEligible() {
        String personalNumber = "some-user";
        HealthInsurance insurance = new HealthInsurance();

        when(repository.findByPersonalNumber(personalNumber)).thenReturn(Optional.of(List.of(insurance)));
        when(toggleService.isDiscountEnabledFor(personalNumber)).thenReturn(false);

        PersonInsuranceResponse response = insuranceService.getInsurancesFor(personalNumber);

        assertEquals(InsuranceType.HEALTH.getMonthlyCost(), response.totalCost());
        assertNull(response.discountedTotalCost());
    }

    @Test
    void shouldFallbackGracefullyWhenVehicleServiceIsDown() {
        String personalNumber = "199001011234";
        CarInsurance insurance = CarInsurance.carInsurance("ABC123");

        when(repository.findByPersonalNumber(personalNumber)).thenReturn(Optional.of(List.of(insurance)));
        when(vehicleWebClient.get()
                .uri("/ABC123", "ABC123")
                .retrieve()
                .onStatus(any(), any())
                .bodyToMono(Vehicle.class)
        ).thenReturn(Mono.error(new RuntimeException("Service unavailable")));
        when(toggleService.isDiscountEnabledFor(personalNumber)).thenReturn(false);

        PersonInsuranceResponse response = insuranceService.getInsurancesFor(personalNumber);

        CarInsurance result = (CarInsurance) response.insurances().getFirst();
        assertNull(result.vehicle());
    }

}

