package com.learn.splitwise.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Splitwise Apis",
                version = "1.0",
                description = "Backend API for managing group expenses and balances"
        ),
        tags = {
                @Tag(name = "Health", description = "Health Check APIs"),
                @Tag(name = "User", description = "User Management APIs"),
                @Tag(name = "Group", description = "Group Management APIs"),
                @Tag(name = "Expense", description = "Expense Management APIs"),
                @Tag(name = "Settle", description = "Settle Management APIs")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}
