import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Make sure that tests without the {@link com.github.t1.jaxrsclienttest.JaxRsTestExtension} still run */
public class NonJaxRsTest {
    @Test void shouldRun() {
        assertThat(true).isTrue();
    }
}
