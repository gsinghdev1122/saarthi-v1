package com.csd.canteen.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI canteenOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Canteen SAARTHI API")
                .description("Multi-canteen CSD operations desk: imports, inventory, workforce, finance, approvals, reporting.")
                .version("1.0.0")
                .contact(new Contact().name("Canteen SAARTHI")));
    }
}
