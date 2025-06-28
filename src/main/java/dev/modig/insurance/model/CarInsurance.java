package dev.modig.insurance.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Car insurance with vehicle enrichment")
public record CarInsurance(

        @Schema(description = "Monthly cost for car insurance", example = "30")
        int monthlyCost,

        @Schema(description = "Car registration number", example = "ABC123")
        @JsonProperty String registrationNumber,

        @Schema(description = "Vehicle details associated with the insurance")
        Vehicle vehicle

) implements Insurance {

    public CarInsurance(String registrationNumber, Vehicle vehicle) {
        this(InsuranceType.CAR.getMonthlyCost(), registrationNumber, vehicle);
    }

    public static CarInsurance carInsurance(String registrationNumber) {
        return new CarInsurance(InsuranceType.CAR.getMonthlyCost(), registrationNumber, null);
    }

    @Override
    public InsuranceType type() {
        return InsuranceType.CAR;
    }

    @JsonProperty
    @Override
    public int monthlyCost() {
        return type().getMonthlyCost();
    }
}
