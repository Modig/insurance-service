package dev.modig.insurance.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class InsuranceServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnInsurancesWhenValidPersonalNumberExists() throws Exception {
        mockMvc.perform(get("/api/v1/insurance/19900101-1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.personalNumber").value("199001011234"))
                .andExpect(jsonPath("$.insurances", hasSize(3)))
                .andExpect(jsonPath("$.insurances[?(@.type == 'HEALTH')].monthlyCost", hasItem(20)))
                .andExpect(jsonPath("$.insurances[?(@.type == 'CAR')].registrationNumber",
                        containsInAnyOrder("ABC123", "XYZ789")))
                .andExpect(jsonPath("$.totalCost").value(80))
                .andExpect(jsonPath("$.discountedTotalCost").value(72));
    }

    @Test
    void shouldReturnBadRequestWhenInvalidPersonalNumber() throws Exception {
        mockMvc.perform(get("/api/v1/insurance/invalid-input"))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason(containsString("Invalid personal number")));
    }

    @Test
    void shouldReturnNotFoundWhenNoInsuranceExists() throws Exception {
        mockMvc.perform(get("/api/v1/insurance/19900101-9999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Insurance not found"));
    }
}
