package fr.insalyon.creatis.vip.core.integrationtest;

import fr.insalyon.creatis.vip.core.server.SpringInternalApiConfig;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.TestPropertySource;

/**
 * Base Class to test the internal API with mocked http requests
 * See {@link BaseWebSpringIT}
 * and ({@link fr.insalyon.creatis.vip.core.integrationtest.database.BaseSpringIT}
 * for more info on configuration.
 *
 * In particular, this will pick up automatically all beans / controllers available in the module where this will be
 * used.
 */
@ContextHierarchy(
        @ContextConfiguration(name="internal-api", classes = SpringInternalApiConfig.class)
)
@TestPropertySource(properties = {"test.servletPath=internal"})
abstract public class BaseInternalApiSpringIT extends BaseWebSpringIT {

    @BeforeEach
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
}
