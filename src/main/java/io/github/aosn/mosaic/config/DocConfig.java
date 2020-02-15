/*
 * Copyright (C) 2017-2020 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.config;

import io.github.aosn.mosaic.MosaicApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Enables Swagger API documentation.
 *
 * @author mikan
 * @since 0.3
 */
@Configuration
@EnableSwagger2
public class DocConfig {

    @Bean
    public Docket document() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("io.github.aosn.mosaic.controller"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(new ApiInfo("Mosaic Web API", "Web API documentation for Mosaic",
                        MosaicApplication.MOSAIC_VERSION, null,
                        "Alice on Sunday Nights Workshop",
                        null, null));
    }
}
