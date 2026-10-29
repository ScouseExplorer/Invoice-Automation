package com.metro.invoice_tool;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CustomerControllerTest {

    @LocalServerPort
    private int port;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Test
    void shouldCreateAndFetchCustomer() throws Exception {
        String createBody = """
                {
                  "name": "Acme Corp",
                  "email": "accounts@acme.com",
                  "phone": "+1-555-0100"
                }
                """;

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl("/api/customers")))
                .header("Content-Type", APPLICATION_JSON.toString())
                .POST(HttpRequest.BodyPublishers.ofString(createBody))
                .build();

        HttpResponse<String> createResponse = httpClient.send(createRequest, HttpResponse.BodyHandlers.ofString());

        assertThat(createResponse.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(createResponse.body()).contains("\"name\":\"Acme Corp\"");

        long id = extractId(createResponse.body());

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl("/api/customers/" + id)))
                .GET()
                .build();

        HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());

        assertThat(getResponse.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(getResponse.body()).contains("\"email\":\"accounts@acme.com\"");
    }

    private String baseUrl(String path) {
        return "http://localhost:" + port + path;
    }

    private long extractId(String responseBody) {
        Pattern idPattern = Pattern.compile("\\\"id\\\"\\s*:\\s*(\\d+)");
        Matcher matcher = idPattern.matcher(responseBody);
        assertThat(matcher.find()).as("id should be present in response").isTrue();
        return Long.parseLong(matcher.group(1));
    }
}
