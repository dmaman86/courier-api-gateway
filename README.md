# Courier Api Gateway

This micro-service serves as the API Gateway for the **courier-app** system. Built using **Spring Cloud Gateway**, it functions as the central entry point for all other micro-services, handling routing and load balancing by forwarding incoming requests to the appropriate service.

## Key Features

- Centralized API routing.
- Load balancing and request filtering.
- Customizable route predicates and filters.

## Technologies Used

- Java 17
- Spring Boot
- Spring Cloud Gateway
- Maven

## Prerequisites

- Java 17 installed.
- Kafka must be running since the backend depends on it.
- MySQL is not required for this service.

## Installation and Running

1. Clone the repository:

   ```bash
   git clone https://github.com/dmaman86/courier-api-gateway.git
   cd courier-api-gateway
   ```

2. Build the project:

   ```bash
   mvn clean install
   ```

3. Run the service:

   ```bash
   mvn spring-boot:run
   ```

The API Gateway will be available at: [http://localhost:8080](http://localhost:8080)

## Routing Configuration

The routing configuration is defined in the `GatewayConfig` class instead of the `application.yml` file. Here is an example of how the routes are defined:

```java
@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
      return builder
          .routes()
          .route("auth-service", r -> r.path("/api/auth/**").uri("lb://courier-auth-service"))
          .route("user-service", r -> r.path("/api/user/**").uri("lb://courier-user-service"))
          .route(
            "resource-service",
            r -> r.path("/api/resource/**").uri("lb://courier-resource-service"))
          .build();
  }
}
```

## Usage

- The API Gateway routes requests to the appropriate micro-service based on the defined path patterns in the `GatewayConfig` class.
- Each micro-service is responsible for validating tokens and handling authorization.
- Unauthorized requests are handled directly by the respective micro-services.

## Contributing

Contributions are welcome. Please open an issue or submit a pull request.

## License

This project is licensed under the [MIT License](LICENSE).

