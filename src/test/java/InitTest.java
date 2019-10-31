import com.github.t1.jaxrsclienttest.JaxRsTestExtension;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class InitTest {
    @Test
    void shouldFailWithClassPassedAsSingleton() {
        Throwable thrown = catchThrowable(() -> new JaxRsTestExtension(InitTest.class));

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class)
            .hasMessageEndingWith("the singleton <class InitTest> passed into JaxRsTestExtension is a class, not an instance");
    }
}
