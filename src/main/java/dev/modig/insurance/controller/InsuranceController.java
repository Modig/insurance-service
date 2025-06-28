package dev.modig.insurance.controller;

import dev.modig.insurance.dto.PersonInsuranceResponse;
import dev.modig.insurance.service.InsuranceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * REST controller for managing insurance-related queries.
 * Provides endpoints to retrieve insurance details for a person using their personal number.
 */
@RestController
@RequestMapping("/api/v1/insurance")
public class InsuranceController {

    private final InsuranceService insuranceService;

    /**
     * Constructs an InsuranceController with the given InsuranceService.
     *
     * @param service the service layer used to handle insurance logic
     */
    public InsuranceController(InsuranceService service) {
        this.insuranceService = service;
    }


    /**
     * Retrieves all insurance information for a specific person based on their personal number.
     *
     * @param personalNumber the personal number of the individual (can contain dash)
     * @return a response entity with the insurance information or an error response
     */
    @Operation(summary = "Get all insurances for a person")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Insurances retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PersonInsuranceResponse.class))),
            @ApiResponse(responseCode = "404", description = "No insurance found for personal number"),
            @ApiResponse(responseCode = "400", description = "Invalid personal number format")
    })
    @GetMapping("/{personalNumber}")
    public ResponseEntity<?> getInsuranceInfo(@PathVariable("personalNumber") String personalNumber) {
        String strippedPersonalNumber = personalNumber.replace("-", "");
        if (!strippedPersonalNumber.matches("^[0-9]{1,12}$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid personal number");
        }

        try {
            return ResponseEntity.ok(insuranceService.getInsurancesFor(strippedPersonalNumber));
        } catch (dev.modig.insurance.exception.InsuranceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Insurance not found");
        }
    }
}
