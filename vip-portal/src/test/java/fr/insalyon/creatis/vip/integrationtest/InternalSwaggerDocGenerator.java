package fr.insalyon.creatis.vip.integrationtest;

import fr.insalyon.creatis.vip.api.data.UserTestUtils;
import fr.insalyon.creatis.vip.core.integrationtest.BaseInternalApiSpringIT;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This is a special test that starts the spring context with the springdoc library.
 * The specific "springdoc" profile ensures the TestSpringDocConfiguration is only added in this test.
 * This test then fetches and copies the json doc in the good place (in webapp/api-doc).
 * A swagger-ui site is then needed to have a nice HTML doc based on it.
 */

@ContextHierarchy(
        @ContextConfiguration(name="internal-api", classes = TestSpringDocConfiguration.class)
)
@ActiveProfiles("springdoc")
public class InternalSwaggerDocGenerator extends BaseInternalApiSpringIT {

    @Test
    public void generateSwaggerJson() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                        .get("/internal/v3/api-docs")
                        .with(UserTestUtils.baseUser1()))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        String swaggerJsonString = mvcResult.getResponse().getContentAsString();
        Files.writeString(Path.of("src/main/webapp/api-doc/internal-api-doc.json"), swaggerJsonString, StandardCharsets.UTF_8);
    }

}
