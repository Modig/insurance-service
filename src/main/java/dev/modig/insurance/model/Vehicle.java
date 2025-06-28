package dev.modig.insurance.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Represents a vehicle registered in the system")
public record Vehicle(@Schema(description = "Registration number", example = "ABC123") String registrationNumber,

                      @Schema(description = "Make of the vehicle", example = "Toyota") String make,

                      @Schema(description = "Model of the vehicle", example = "Camry") String model,

                      @Schema(description = "Year of manufacture", example = "2018", minimum = "1886", maximum = "2099") int year) {
}
