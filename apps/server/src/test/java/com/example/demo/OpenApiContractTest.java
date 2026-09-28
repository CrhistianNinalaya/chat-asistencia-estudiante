package com.example.demo;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class OpenApiContractTest {

    @Autowired
    private WebApplicationContext context;

    @Test
    @DisplayName("Export OpenAPI JSON specification to build directory")
    void shouldExportOpenApiSpecification() throws Exception {
        // Arrange
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        Path outputDir = Path.of("build");
        Files.createDirectories(outputDir);

        // Act
        String openApiJson = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Assert
        Files.writeString(outputDir.resolve("openapi.json"), openApiJson, StandardCharsets.UTF_8);
    }
}
