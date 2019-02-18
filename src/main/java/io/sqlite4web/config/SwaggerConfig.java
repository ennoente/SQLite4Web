package io.sqlite4web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

@EnableSwagger2
@Configuration
public class SwaggerConfig {

    @Bean
    public Docket sqlite4webAPI() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("io.sqlite4web"))
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfo(
                "SQLite4Web REST API",
                "The API for SQLite4Web version 1.1.",
                "1.1",
                "Terms of service",
                new Contact("Enno Thoma", "https://ennothoma.io", "contact@ennothoma.io"),
                "License of API", "API license URL", Collections.emptyList());
    }
}
