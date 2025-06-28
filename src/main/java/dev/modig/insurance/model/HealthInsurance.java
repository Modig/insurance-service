package dev.modig.insurance.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Health insurance policy")
public record HealthInsurance() implements Insurance {

    @Override
    public InsuranceType type() {
        return InsuranceType.HEALTH;
    }

    @Override
    @JsonProperty
    @Schema(description = "Monthly cost for health insurance", example = "20")
    public int monthlyCost() {
        return type().getMonthlyCost();
    }
}
