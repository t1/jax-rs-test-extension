import com.github.t1.jaxrsclienttest.JaxRsTestExtension;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.net.URI;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static org.assertj.core.api.BDDAssertions.then;

class GatewayTest {

    @RequiredArgsConstructor
    public static class MyGateway {
        final Client client;
        final URI baseUri;

        public String getFoo() {
            Response response = client.target(baseUri).request(TEXT_PLAIN_TYPE).get();
            assert 200 == response.getStatus();
            return response.readEntity(String.class);
        }
    }

    @Path("/") public static class MockService {
        @GET public String get() { return "foo"; }
    }

    @RegisterExtension static JaxRsTestExtension jaxRs = new JaxRsTestExtension(new MockService());

    @Test void shouldGet() {
        MyGateway gateway = new MyGateway(jaxRs.client(), jaxRs.baseUri());

        String foo = gateway.getFoo();

        then(foo).isEqualTo("foo");
    }
}
