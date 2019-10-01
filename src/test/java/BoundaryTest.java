import com.github.t1.jaxrsclienttest.JaxRsTestExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.BDDAssertions.then;

class BoundaryTest {

    @Path("/") public static class MockBoundary {
        @GET public String get() { return "foo"; }
    }

    @RegisterExtension static JaxRsTestExtension jaxRs = new JaxRsTestExtension(new MockBoundary());

    @Test void shouldGet() {
        Response response = jaxRs.GET("/");

        then(response.getStatusInfo()).isEqualTo(OK);
        then(response.readEntity(String.class)).isEqualTo("foo");
    }
}
