package dev.modig.insurance.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.modig.insurance.model.Insurance;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PersonInsuranceResponse(String personalNumber, List<Insurance> insurances, int totalCost,
                                      Integer discountedTotalCost) {
}

