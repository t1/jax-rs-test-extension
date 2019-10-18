import com.github.t1.jaxrsclienttest.JaxRsTestExtension;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.json.JsonObject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.assertj.core.api.BDDAssertions.then;

class NestedTest {
    public static class ApiClient {
        public boolean foo() {
            Response response = JAX_RS.GET("/");
            JsonObject json = response.readEntity(JsonObject.class);
            return json.getBoolean("foo");
        }
    }

    private ApiClient client = new ApiClient();

    @Nested class NestedA {
        @Test void shouldRead() {
            body = "{\"foo\":true}";

            boolean foo = client.foo();

            then(foo).isTrue();
        }
    }

    @Nested class NestedB {
        @Test void shouldRead() {
            body = "{\"foo\":false}";

            boolean foo = client.foo();

            then(foo).isFalse();
        }
    }


    static final @RegisterExtension JaxRsTestExtension JAX_RS = new JaxRsTestExtension(new ServiceDummy());

    private static String body;

    @Path("/")
    public static class ServiceDummy {
        @Produces(APPLICATION_JSON) @GET public String get() { return body; }
    }
}
