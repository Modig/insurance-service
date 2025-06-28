package dev.modig.insurance.service;

import dev.modig.insurance.dto.PersonInsuranceResponse;
import dev.modig.insurance.exception.InsuranceNotFoundException;
import dev.modig.insurance.exception.VehicleNotFoundException;
import dev.modig.insurance.model.CarInsurance;
import dev.modig.insurance.model.Insurance;
import dev.modig.insurance.model.Vehicle;
import dev.modig.insurance.repository.InsuranceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class InsuranceService {

    private static final Logger log = LoggerFactory.getLogger(InsuranceService.class);
    private final InsuranceRepository repository;
    private final WebClient vehicleWebClient;
    private final ToggleService toggleService;

    public InsuranceService(InsuranceRepository repository, WebClient vehicleWebClient, ToggleService toggleService) {
        this.repository = repository;
        this.vehicleWebClient = vehicleWebClient;
        this.toggleService = toggleService;
    }

    /**
     * Retrieves and processes all insurances for a given personal number.
     * <p>
     * This method:
     * <ul>
     *   <li>Fetches raw insurance data from the repository</li>
     *   <li>Enriches car insurances with vehicle details via WebClient</li>
     *   <li>Calculates total monthly cost of all insurances</li>
     *   <li>Applies a discount if the user is eligible via ToggleService</li>
     * </ul>
     *
     * @param personalNumber a 12-digit Swedish personal number (YYYYMMDDNNNN)
     * @return a response object containing all enriched insurances and calculated costs
     * @throws InsuranceNotFoundException if no insurances are found
     */
    public PersonInsuranceResponse getInsurancesFor(String personalNumber) {
        List<Insurance> rawInsurances = repository.findByPersonalNumber(personalNumber)
                .filter(list -> !list.isEmpty())
                .orElseThrow(() -> new InsuranceNotFoundException(personalNumber));

        List<Insurance> enriched = rawInsurances.stream()
                .map(this::enrichIfCarInsurance)
                .toList();

        int totalCost = enriched.stream()
                .mapToInt(Insurance::monthlyCost)
                .sum();

        Integer discountedTotal = null;
        if (toggleService.isDiscountEnabledFor(personalNumber)) {
            discountedTotal = (int) Math.round(totalCost * 0.9);
        }

        return new PersonInsuranceResponse(personalNumber, enriched, totalCost, discountedTotal);
    }

    private Insurance enrichIfCarInsurance(Insurance insurance) {
        if (insurance instanceof CarInsurance car) {
            try {
                Vehicle vehicle = fetchVehicleFor(car.registrationNumber());
                return new CarInsurance(car.registrationNumber(), vehicle);
            } catch (Exception e) {
                log.warn("Failed to fetch vehicle for {}: {}", car.registrationNumber(), e.getMessage());
                return new CarInsurance(car.registrationNumber(), null);
            }
        }
        return insurance;
    }

    private Vehicle fetchVehicleFor(String registrationNumber) {
        return vehicleWebClient.get()
                .uri("/{registrationNumber}", registrationNumber)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> Mono.error(new VehicleNotFoundException("Vehicle not found")))
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new RuntimeException("Vehicle service down")))
                .bodyToMono(Vehicle.class)
                .block();
    }
}
