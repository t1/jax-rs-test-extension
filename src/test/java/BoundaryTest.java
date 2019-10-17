import com.github.t1.jaxrsclienttest.JaxRsTestExtension;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.BDDAssertions.then;

class BoundaryTest {

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class Entity {

        String first, second;
    }

    @Path("/") public static class MockBoundary {

        @GET public Entity get() { return ENTITY; }
    }

    @RegisterExtension static JaxRsTestExtension jaxRs = new JaxRsTestExtension(new MockBoundary());

    @Test void shouldGet() {
        Response response = jaxRs.GET("/");

        then(response.getStatusInfo()).isEqualTo(OK);
        response.bufferEntity();
        then(response.readEntity(String.class)).isEqualTo("{\"first\":\"A\",\"second\":\"B\"}");
        then(response.readEntity(Entity.class)).isEqualTo(ENTITY);
    }

    private static final Entity ENTITY = new Entity("A", "B");
}
