package dev.modig.insurance.repository;

import dev.modig.insurance.model.CarInsurance;
import dev.modig.insurance.model.HealthInsurance;
import dev.modig.insurance.model.Insurance;
import dev.modig.insurance.model.PetInsurance;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class InsuranceRepository {

    private final Map<String, List<Insurance>> insuranceRegistry = new HashMap<>();

    public InsuranceRepository() {
        insuranceRegistry.put("199001011234", List.of(
                new HealthInsurance(),
                CarInsurance.carInsurance("ABC123"),
                CarInsurance.carInsurance("XYZ789")
        ));

        insuranceRegistry.put("200101010023", List.of(
                new PetInsurance()
        ));

        insuranceRegistry.put("198505055678", List.of(
                new PetInsurance()
        ));

        insuranceRegistry.put("200002024321", List.of(
                new HealthInsurance(),
                new PetInsurance()
        ));

        insuranceRegistry.put("190101010015", List.of(
                new HealthInsurance(),
                new PetInsurance(),
                new PetInsurance()
        ));

        insuranceRegistry.put("197707078888", List.of(
                CarInsurance.carInsurance("AUD00I")
        ));

        insuranceRegistry.put("190101010023", List.of(
                CarInsurance.carInsurance("UNKNOWN")
        ));
    }

    public Optional<List<Insurance>> findByPersonalNumber(String personalNumber) {
        return Optional.ofNullable(insuranceRegistry.get(personalNumber.toUpperCase()));
    }
}
