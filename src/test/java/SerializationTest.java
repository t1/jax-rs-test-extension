import com.github.t1.jaxrsclienttest.JaxRsTestExtension;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.Optional;

import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;

public class SerializationTest {

    @Data
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static class Person {
        String firstName = "Joe";
        String lastName = null;
        Optional<String> phone = Optional.of("123");
        Optional<String> fax = Optional.empty();
        LocalDate born = LocalDate.of(2019, 10, 1);
    }

    public static final Person PERSON = new Person();

    public static final String JSON = "{\"born\":\"2019-10-01\",\"firstName\":\"Joe\",\"phone\":\"123\"}";

    @Path("/") public static class PersonBoundary {
        @GET @Path("/serialize") public Person getSerialize() { return PERSON; }

        @GET @Path("/deserialize") public String getDeserialize() {
            return JSON.replace("}", ",\"unknownField\":\"foo\"}");
        }
    }

    @RegisterExtension static JaxRsTestExtension jaxRs = new JaxRsTestExtension(new PersonBoundary());

    @Test void shouldGetSerialize() {
        Response response = jaxRs.GET("/serialize");

        assertThat(response.getStatusInfo()).isEqualTo(OK);
        assertThat(response.readEntity(String.class)).isEqualTo(JSON);
    }

    @Test void shouldGetDeserialize() {
        Response response = jaxRs.GET("/deserialize");

        assertThat(response.getStatusInfo()).isEqualTo(OK);
        assertThat(response.readEntity(Person.class)).isEqualTo(PERSON);
    }
}
