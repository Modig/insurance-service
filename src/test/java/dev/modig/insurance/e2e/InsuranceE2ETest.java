package dev.modig.insurance.e2e;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.modig.insurance.InsuranceServiceApplication;
import dev.modig.insurance.dto.PersonInsuranceResponse;
import dev.modig.insurance.model.CarInsurance;
import dev.modig.insurance.model.HealthInsurance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = InsuranceServiceApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class InsuranceE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/v1/insurance";
    }

    @Test
    void testGetInsurancesSuccessWithDiscount() {
        ResponseEntity<PersonInsuranceResponse> response = restTemplate.getForEntity(
                baseUrl() + "/19900101-1234", PersonInsuranceResponse.class);

        PersonInsuranceResponse body = response.getBody();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(body).isNotNull();
        assertThat(body.personalNumber()).isEqualTo("199001011234");
        assertThat(body.totalCost()).isGreaterThan(0);
        assertThat(body.discountedTotalCost()).isNotNull();
        assertThat(body.insurances()).hasSizeGreaterThanOrEqualTo(2);

        boolean containsCar = body.insurances().stream().anyMatch(i -> i instanceof CarInsurance);
        boolean containsHealth = body.insurances().stream().anyMatch(i -> i instanceof HealthInsurance);

        assertThat(containsCar).isTrue();
        assertThat(containsHealth).isTrue();
    }

    @Test
    void testGetInsurancesNoDiscount() {
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl() + "/19900101-9999", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("Insurance not found");
    }

    @SuppressWarnings("unchecked")
    @Test
    void testInvalidPersonalNumber() throws JsonProcessingException {
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl() + "/invalid-input", String.class);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> body = mapper.readValue(response.getBody(), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(body.get("path")).isEqualTo("/api/v1/insurance/invalid-input");
    }

    @Test
    void testInsuranceNotFound() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl() + "/19900101-9999", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("Insurance not found");
    }
}
