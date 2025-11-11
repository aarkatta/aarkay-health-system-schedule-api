package com.aarkay.health.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for HAPI FHIR client.
 * Provides beans for FhirContext and IGenericClient to interact with FHIR servers.
 */
@Configuration
public class FhirConfig {

    @Value("${fhir.server.base-url}")
    private String fhirServerBaseUrl;

    @Value("${fhir.client.connection-timeout}")
    private Integer connectionTimeout;

    @Value("${fhir.client.socket-timeout}")
    private Integer socketTimeout;

    /**
     * Creates a singleton FhirContext for FHIR R4.
     * FhirContext is thread-safe and should be reused.
     *
     * @return FhirContext configured for FHIR R4
     */
    @Bean
    public FhirContext fhirContext() {
        FhirContext ctx = FhirContext.forR4();

        // Disable server validation on startup for better performance
        ctx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);

        // Set connection timeouts
        ctx.getRestfulClientFactory().setConnectTimeout(connectionTimeout);
        ctx.getRestfulClientFactory().setSocketTimeout(socketTimeout);

        return ctx;
    }

    /**
     * Creates a generic FHIR client bean for making API calls.
     *
     * @param fhirContext the FHIR context
     * @return IGenericClient configured to connect to the FHIR server
     */
    @Bean
    public IGenericClient fhirClient(FhirContext fhirContext) {
        return fhirContext.newRestfulGenericClient(fhirServerBaseUrl);
    }
}
