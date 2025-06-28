package dev.modig.insurance.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents a generic insurance product.
 * Implementing classes define specific types of insurance such as car, health, or pet insurance.
 */
@Schema(
        description = "Base interface for all insurance types",
        subTypes = {CarInsurance.class, HealthInsurance.class, PetInsurance.class},
        discriminatorProperty = "type"
)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CarInsurance.class, name = "CAR"),
        @JsonSubTypes.Type(value = HealthInsurance.class, name = "HEALTH"),
        @JsonSubTypes.Type(value = PetInsurance.class, name = "PET")
})
public sealed interface Insurance permits CarInsurance, HealthInsurance, PetInsurance {

    /**
     * Returns the type of this insurance.
     *
     * @return the insurance type
     */
    @Schema(description = "Type of insurance", example = "CAR")
    InsuranceType type();

    /**
     * Returns the monthly cost of this insurance.
     *
     * @return the monthly premium in currency units
     */
    @Schema(description = "Monthly cost of the insurance", example = "30")
    int monthlyCost();
}
