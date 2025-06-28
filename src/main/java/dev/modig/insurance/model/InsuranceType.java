package dev.modig.insurance.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enum representing different types of insurance.
 * Each type has an associated monthly cost.
 */
@Schema(description = "Enumeration of available insurance types")
public enum InsuranceType {

    @Schema(description = "Pet insurance", example = "PET")
    PET(10),

    @Schema(description = "Health insurance", example = "HEALTH")
    HEALTH(20),

    @Schema(description = "Car insurance", example = "CAR")
    CAR(30);

    private final int monthlyCost;

    /**
     * Constructs an InsuranceType with a specified monthly cost.
     *
     * @param monthlyCost the monthly cost of this insurance type
     */
    InsuranceType(int monthlyCost) {
        this.monthlyCost = monthlyCost;
    }

    /**
     * Returns the monthly cost associated with this insurance type.
     *
     * @return the monthly premium in currency units
     */
    @Schema(description = "Monthly cost associated with this insurance type")
    public int getMonthlyCost() {
        return monthlyCost;
    }
}
