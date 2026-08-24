package fr.insalyon.creatis.vip.integrationtest;

import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration needed for springdoc : scan "org.springdoc" and declare a few config beans
 * Only needed in the SwaggerDocGenerator test, and activated by the specific "springdoc" profile
 */

@Configuration
@Profile("springdoc")
@ComponentScan(basePackages = "org.springdoc")
public class TestSpringDocConfiguration {

    @Value("${test.servletPath}")
    private String servletPath;

    @Bean
    public SpringDocConfigProperties springDocConfigProperties() {
        SpringDocConfigProperties properties = new SpringDocConfigProperties();
        SpringDocConfigProperties.ApiDocs apiDocs = new SpringDocConfigProperties.ApiDocs();
        // the `/internal/` need to be specified
        // for swagger-ui config generation
        apiDocs.setPath("/"+servletPath+"/v3/api-docs");

        properties.setApiDocs(apiDocs);
        properties.setWriterWithDefaultPrettyPrinter(true);

        return properties;
    }

    @Bean
    public WebProperties springDocWebProperties() {
        return new WebProperties();
    }

    @Bean
    public WebMvcProperties springDocWebMvcProperties() {
        return new WebMvcProperties();
    }

}
