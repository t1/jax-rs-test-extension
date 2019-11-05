package com.github.t1.jaxrsclienttest;

import io.undertow.Undertow;
import io.undertow.Undertow.ListenerInfo;
import io.undertow.servlet.api.DeploymentInfo;
import lombok.SneakyThrows;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

/**
 * JUnit 5 Jupiter Extension to make it easy to test JAX-RS infrastructure code.
 */
public class JaxRsTestExtension implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback, Extension {
    private static UndertowJaxrsServer SERVER;
    private static Set<Object> SINGLETONS = null;

    private int port = 0;
    private String contextPath = "/";
    private URI baseUri;
    private int nesting = 0;
    private Client jaxRsClient;

    /** Strangely required by Jupiter for an automatically discovered extension */
    @SuppressWarnings("unused") public JaxRsTestExtension() {}

    /**
     * Pass in all the resources required for the service.
     */
    public JaxRsTestExtension(Object... resources) {
        SINGLETONS = new HashSet<>(asList(resources));
        SINGLETONS.forEach(this::checkSingleton);
    }

    private void checkSingleton(Object singleton) {
        if (singleton instanceof Class)
            throw new IllegalArgumentException("the singleton <" + singleton + "> passed into " + JaxRsTestExtension.class.getSimpleName() +
                " is a class, not an instance");
    }

    /**
     * By default, the port is <code>0</code>, i.e. a random, free port, which is a good thing,
     * as there won't be any port conflicts. But sometimes you need a fixed port.
     */
    public JaxRsTestExtension port(int port) {
        this.port = port;
        return this;
    }

    /**
     * By default, the application path "/" is used, i.e. there is no prefix to all requests.
     */
    public JaxRsTestExtension contextPath(String contextPath) {
        this.contextPath = contextPath;
        return this;
    }


    @Override public void beforeAll(ExtensionContext context) { start(); }

    @Override public void beforeEach(ExtensionContext context) { start(); }

    @Override public void afterEach(ExtensionContext context) { stop(); }

    @Override public void afterAll(ExtensionContext context) { stop(); }

    public void start() {
        nesting++;
        if (nesting == 1) {
            assert SERVER == null;
            SERVER = new UndertowJaxrsServer().start(Undertow.builder().addHttpListener(port, "localhost"));
            this.baseUri = getBaseUri();
            deployDummyApp();
        }
    }

    private URI getBaseUri() {
        // Undertow assumes that the port is always fixed; so we'll have to do some reflection magic
        Undertow undertow = getField(SERVER, "server", Undertow.class);
        ListenerInfo listenerInfo = undertow.getListenerInfo().iterator().next(); // we only have one
        InetSocketAddress socketAddress = (InetSocketAddress) listenerInfo.getAddress();
        return URI.create(listenerInfo.getProtcol() + "://" + socketAddress.getHostString() + ":" + socketAddress.getPort());
    }

    @SuppressWarnings("SameParameterValue") @SneakyThrows(ReflectiveOperationException.class)
    private static <T> T getField(Object instance, String fieldName, Class<T> type) {
        Field field = instance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return type.cast(field.get(instance));
    }

    private void deployDummyApp() {
        Class<?> firstResource = SINGLETONS.iterator().next().getClass();
        try {
            DeploymentInfo deploymentInfo = SERVER.undertowDeployment(DummyApp.class);
            deploymentInfo.setClassLoader((firstResource.getClassLoader() != null) ? firstResource.getClassLoader() : ClassLoader.getSystemClassLoader());
            deploymentInfo.setContextPath(contextPath);
            deploymentInfo.setDeploymentName(firstResource.getSimpleName());
            SERVER.deploy(deploymentInfo);
        } catch (Throwable e) {
            throw new RuntimeException("can't deploy " + firstResource, e);
        }
    }

    public void stop() {
        nesting--;
        if (nesting == 0) {
            assert SERVER != null;
            SERVER.stop();
            SERVER = null;
            SINGLETONS = null;
        }
    }

    /**
     * The service normally runs on an arbitrary port... here you'll get that info.
     */
    public URI baseUri() { return baseUri; }

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
            jaxRsClient = ClientBuilder.newClient();
        return jaxRsClient;
    }

    public static class DummyApp extends Application {
        @Override public Set<Object> getSingletons() { return SINGLETONS; }
    }
}
