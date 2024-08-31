package com.example.backend4frontend

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@OpenAPIDefinition(
    info = Info(
        title = "Backend 4 Frontend Application",
        version = "1.0.0",
        description = "API for managing tasks"
    )
)
class Backend4frontendApplication

fun main(args: Array<String>) {
    runApplication<Backend4frontendApplication>(*args)
}
