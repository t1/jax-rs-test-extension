package com.github.t1.jaxrsclienttest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import io.dropwizard.testing.common.DropwizardClient;
import org.glassfish.jersey.client.ClientConfig;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.net.URI;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider.DEFAULT_ANNOTATIONS;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

/**
 * JUnit 5 Jupiter Extension to make it easy to test JAX-RS infrastructure code.
 */
public class JaxRsTestExtension implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback {
    private final DropwizardClient dropwizardClient;
    private Client jaxRsClient;
    private boolean isStatic = false;

    /**
     * Pass in all the resources required for the service.
     */
    public JaxRsTestExtension(Object... resources) {
        this.dropwizardClient = new DropwizardClient(resources);
    }

    @Override public void beforeAll(ExtensionContext context) {
        isStatic = true;
        launch();
    }

    @Override public void beforeEach(ExtensionContext context) {
        if (!isStatic)
            launch();
    }

    @Override public void afterEach(ExtensionContext context) {
        if (!isStatic)
            dropwizardClient.after();
    }

    @Override public void afterAll(ExtensionContext context) {
        if (isStatic)
            dropwizardClient.after();
    }

    private void launch() {
        start();
        configureJackson();
    }

    private void start() {
        try {
            dropwizardClient.before();
        } catch (Throwable e) {
            throw new RuntimeException("can't boot dropwizard client", e);
        }
    }

    private void configureJackson() {
        getObjectMapper()
            .disable(WRITE_DATES_AS_TIMESTAMPS)
            .disable(FAIL_ON_UNKNOWN_PROPERTIES)
            .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule());
    }

    private ObjectMapper getObjectMapper() {
        return dropwizardClient.getObjectMapper();
    }

    /**
     * The service runs on an arbitrary port... here you'll get that info.
     */
    public URI baseUri() { return dropwizardClient.baseUri(); }

    /**
     * Convenience method to do a GET on the given `path` with `Accept: application/json`.
     */
    public Response GET(String path) {
        return target().path(path).request(APPLICATION_JSON_TYPE).get();
    }

    /**
     * Build a {@link WebTarget} with the {@link #baseUri()}.
     */
    public WebTarget target() { return client().target(baseUri()); }

    /**
     * Build a {@link WebTarget} and cache it.
     */
    public Client client() {
        if (jaxRsClient == null)
            jaxRsClient = buildJaxRsClient();
        return jaxRsClient;
    }

    private Client buildJaxRsClient() {
        JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider(getObjectMapper(), DEFAULT_ANNOTATIONS);
        return ClientBuilder.newClient(new ClientConfig(provider));
    }
}
