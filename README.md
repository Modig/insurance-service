# Insurance Service

The **Insurance Service** is a Spring Boot microservice that exposes a REST API for retrieving insurance information by
personal identification number. It integrates with the Vehicle Service to enrich car insurance records with vehicle
details.

---

## Features

- Exposes a REST endpoint to retrieve all insurances for a given person.
- Supports pet, health, and car insurance types.
- Enriches car insurances with vehicle data via HTTP integration.
- Calculates total and discounted monthly costs.
- Supports runtime-configurable feature toggles using FF4j.
- Implements gradual rollout and user-based toggle targeting.
- Includes Swagger / OpenAPI documentation.

---

## Getting Started

### Prerequisites

- Java 21
- Maven 3.8+
- Git
- [Vehicle Service](https://github.com/modig/vehicle-service) running locally or accessible via network

### Clone the Repository

```
git clone https://github.com/modig/insurance-service.git
cd insurance-service
```

### Run Tests

Run all tests:

```
mvn clean verify
```

### Run the Application

```
mvn spring-boot:run
```

The application will start on port **8081** by default. Configurable in `application.yml`.

---

## API Documentation

Swagger/OpenAPI documentation is available at:

* Swagger UI: [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html)
* OpenAPI Spec: [http://localhost:8081/v3/api-docs](http://localhost:8081/v3/api-docs)

---

## Feature Toggle Console (FF4j)

Feature toggles are powered by **FF4j** and accessible via:

* FF4j Web Console: [http://localhost:8081/ff4j-web-console](http://localhost:8081/ff4j-web-console)

Default feature flags:

* `DISCOUNT_CAMPAIGN`: Enables a 10% discount for eligible users.

Toggle targeting includes:

* Explicit user list (`TOGGLED_USERS`)
* 20% hash-based canary rollout group

---

## API Usage

### GET `/api/v1/insurance/{personalNumber}`

**Description**: Returns all insurances for a user and computes total (and optionally discounted) monthly cost.

**Personal number format**: Can include a dash (e.g., `19900101-1234`), which will be normalized.

**Sample Request**:

```http
GET /api/v1/insurance/19900101-1234 HTTP/1.1
Host: localhost:8081
```

**Successful Response** (`200 OK`):

```json
{
  "personalNumber": "199001011234",
  "insurances": [
    {
      "type": "HEALTH",
      "monthlyCost": 20
    },
    {
      "type": "CAR",
      "monthlyCost": 30,
      "registrationNumber": "ABC123",
      "vehicle": {
        "registrationNumber": "ABC123",
        "make": "Toyota",
        "model": "Camry",
        "year": 2018
      }
    }
  ],
  "totalCost": 50,
  "discountedTotalCost": 45
}
```

**Validation Error** (`400 Bad Request`):

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid personal number"
}
```

**Not Found** (`404 Not Found`):

```
Insurance not found
```

---

## File Structure (Simplified)

```
insurance-service/
├── src/
│   ├── main/
│   │   └── java/dev/modig/insurance/
│   │       ├── controller/         # REST controller
│   │       ├── service/            # Insurance & toggle logic
│   │       ├── repository/         # In-memory insurance registry
│   │       ├── model/              # Insurance & vehicle types
│   │       ├── exception/          # Custom exceptions
│   │       ├── config/             # FF4j, WebClient configs
│   └── test/                       # Unit, integration, E2E tests
├── pom.xml
└── README.md
```

---

## Integration with Vehicle Service

The Insurance Service fetches vehicle data for car insurances from the Vehicle Service using `WebClient`. Make sure the
Vehicle Service is accessible at the URL specified in `application.yml`:

```yaml
vehicle:
  service:
    url: http://localhost:8080/api/v1/vehicle
```

---

## Build Package

To generate a JAR:

```
mvn clean package
```

The JAR will be in `target/insurance-service-*.jar`.

---

## Configuration

All configuration is in:

```
src/main/resources/application.yml
```

To change the port:

```yaml
server:
  port: 8081
```

To update the vehicle service URL:

```yaml
vehicle:
  service:
    url: http://localhost:8080/api/v1/vehicle
```

---

## CI/CD Integration

This service is CI/CD-ready using GitHub Actions. The pipeline:

* Runs on PRs and pushes.
* Executes tests automatically.
* Deploys to production (currently mocked out and inactive).

See `.github/workflows/ci-cd.yml` for details.

---

## Areas for Improvement

* The insurance registry is hardcoded and should be externalized to a database.
* Authentication and authorization are not implemented.
* Input validation and sanitization are minimal.
* The vehicle fetch is synchronous (`.block()`) and could be optimized for concurrent enrichment.
* Configurable timeouts and error handling could be improved for the Vehicle integration.
* A circuit breaker or retry mechanism could improve robustness when calling external services.
* Contract testing (e.g., with Pact) is not implemented.
* Logging is minimal and should be extended for observability.
* FF4j uses in-memory storage only. Toggle state is not persisted between restarts.
* Toggled users are located in a constant in the code. Could be moved to a for instance a database for dynamic runtime
  changes.
* The canary solution relies on hashCode. Could be improved to use non-implementation specific solutions.
* Domain model leaked to the API (List<Insurances>). Could use response objects instead.
* Additional configuration profiles (e.g., `dev`, `prod`) could be introduced.
* Blue/Green deployment is not implemented.

---

## Discussion

### Architecture and Design

In some sense, this is an overengineered solution. You don’t need five packages to return a single type of hardcoded
vehicle data over an API. However, my goal wasn’t just to solve the assignment, but to model a foundation for something
that could evolve into a full-fledged system by:

- Separating concerns into dedicated layers for data fetching, domain models, feature toggles, business logic, etc.
- Introducing API versioning to make behavior changes easier to manage in a backward-compatible way.
- Integrating a feature toggle mechanism using FF4j to enable controlled rollouts and runtime configuration.

Another thing that could be debated if REST versus Messaging. My experience tells me that each time a microservices call
another microservice it can be taken as a trigger to think about if a messaging solution might be more suitable. Often
it is not, but sometimes it is.

### Feature Toggling

This solution demonstrates two types of feature toggles: runtime toggles via FF4j, and versioning-based toggling at the
API level.
API versioning allows new behavior to be introduced through separate endpoints, effectively “toggling” users between
versions by routing.
The FF4j-based toggle system supports user-targeted toggling, runtime enable/disable through the web console, and basic
canary testing (via `ToggleService.isInCanaryGroup()`).

The application doesn’t currently support blue/green deployments. This is mainly because deployment infrastructure was
scoped out of this assignment. However, the architecture doesn’t prevent such a deployment model from being added in the
future.

### Testing

The application is tested at multiple levels: unit, integration, and end-to-end. Contract testing is not included,
partly
due to time constraints, and partly because I believe such tests are best demonstrated with a broker setup (e.g., Pact),
which would be excessive for this scope.

Tests are integrated into the CI/CD pipeline. A failing test will block deployment, supporting safe delivery.

The test coverage follows the test pyramid fairly well. That said, I believe the classic test pyramid is becoming more
of a guideline than a strict rule, especially as tooling and deployment patterns evolve.

### DevOps

DevOps has been one of the biggest paradigm shifts in software development during my career. I strongly believe in the
value it brings, and I’ve reflected that in this solution by providing:

* Automated tests on multiple levels, integrated into the GitHub workflow.
* CI/CD pipeline support.
* Infrastructure as Code via GitHub Actions.
* Safe releases enforced by test-driven deployment logic (even though the deploy step is currently deactivated).

### Documentation

I believe documentation is important, especially for external APIs, onboarding new team members, and understanding what
I built a year ago. However, documentation can easily become outdated if not managed carefully.

In this solution, I aimed to keep documentation **close to the code** to increase its chance of staying up to date. This
includes:

* Clean and readable code structure.
* Javadoc for key components.
* Auto-generated API documentation with Swagger/OpenAPI.
* This `README.md` file as an accessible overview.

### Personal Reflection

This project closely resembles challenges I’ve tackled in previous roles, for example, during three years of work
decomposing a banking monolith into microservices.

The most interesting part was experimenting with FF4j. This was a new tool for me. In the past, I’ve mostly worked with
in-house feature toggle frameworks.

If I were to take this further, I would focus on resolving the major blockers for production readiness:

* Add real database storage instead of in-memory hardcoded values.
* Introduce authentication and authorization, potentially using Spring Security with OAuth2 and JWT.
* Other security enhancements like input validation/sanitation.
* Add observability and monitoring when deployed
