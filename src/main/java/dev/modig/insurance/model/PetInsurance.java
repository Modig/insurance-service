package dev.modig.insurance.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Pet insurance policy")
public record PetInsurance() implements Insurance {

    @Override
    public InsuranceType type() {
        return InsuranceType.PET;
    }

    @Override
    @JsonProperty
    @Schema(description = "Monthly cost for pet insurance", example = "10")
    public int monthlyCost() {
        return type().getMonthlyCost();
    }
}
