package com.metro.invoice_tool;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InvoiceControllerTest {

        @LocalServerPort
        private int port;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Test
    void shouldCreateAndFetchInvoice() throws Exception {
        String requestBody = """
                {
                  "customerName": "Acme Corp",
                  "amount": 150.0,
                  "issueDate": "2026-10-27"
                }
                """;

            HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl("/api/invoices")))
                .header("Content-Type", APPLICATION_JSON.toString())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            HttpResponse<String> createResponse = httpClient.send(createRequest, HttpResponse.BodyHandlers.ofString());

            assertThat(createResponse.statusCode()).isEqualTo(HttpStatus.CREATED.value());
            assertThat(createResponse.body()).contains("\"customerName\":\"Acme Corp\"");

            long id = extractId(createResponse.body());

            HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl("/api/invoices/" + id)))
                .GET()
                .build();

            HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());

            assertThat(getResponse.statusCode()).isEqualTo(HttpStatus.OK.value());
            assertThat(getResponse.body()).contains("\"amount\":150.0");
    }

        @Test
        void shouldComputeOverdueFromDueDateAndStatus() throws Exception {
                LocalDate today = LocalDate.now();
                LocalDate issueDate = today.minusDays(37);

                String createBody = String.format("""
                                {
                                    "customerName": "Metro Supplies",
                                    "amount": 480.75,
                                    "issueDate": "%s"
                                }
                                """, issueDate);

                HttpRequest createRequest = HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl("/api/invoices")))
                        .header("Content-Type", APPLICATION_JSON.toString())
                        .POST(HttpRequest.BodyPublishers.ofString(createBody))
                        .build();

                HttpResponse<String> createResponse = httpClient.send(createRequest, HttpResponse.BodyHandlers.ofString());
                assertThat(createResponse.statusCode()).isEqualTo(HttpStatus.CREATED.value());

                long id = extractId(createResponse.body());

                String updateBody = String.format("""
                                {
                                    "customerName": "Metro Supplies",
                                    "amount": 480.75,
                                    "issueDate": "%s",
                                    "status": "ISSUED"
                                }
                                """, issueDate);

                HttpRequest updateRequest = HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl("/api/invoices/" + id)))
                        .header("Content-Type", APPLICATION_JSON.toString())
                        .PUT(HttpRequest.BodyPublishers.ofString(updateBody))
                        .build();

                HttpResponse<String> updateResponse = httpClient.send(updateRequest, HttpResponse.BodyHandlers.ofString());
                assertThat(updateResponse.statusCode()).isEqualTo(HttpStatus.OK.value());
                assertThat(updateResponse.body()).contains("\"status\":\"ISSUED\"");
                assertThat(updateResponse.body()).contains("\"overdue\":true");
                assertThat(updateResponse.body()).contains("\"overdueDays\":7");
        }

    @Test
    void shouldReturnOnlyOverdueUnpaidInvoices() throws Exception {
        LocalDate today = LocalDate.now();
        LocalDate overdueIssueDate = today.minusDays(45);
        LocalDate notOverdueIssueDate = today.minusDays(5);

        long overdueIssuedId = createInvoice("Overdue Issued", overdueIssueDate);
        updateInvoiceStatus(overdueIssuedId, "Overdue Issued", overdueIssueDate, "ISSUED");

        long overduePaidId = createInvoice("Overdue Paid", overdueIssueDate);
        updateInvoiceStatus(overduePaidId, "Overdue Paid", overdueIssueDate, "PAID");

        long notOverdueIssuedId = createInvoice("Not Overdue Issued", notOverdueIssueDate);
        updateInvoiceStatus(notOverdueIssuedId, "Not Overdue Issued", notOverdueIssueDate, "ISSUED");

        HttpRequest overdueRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl("/api/invoices/overdue")))
                .GET()
                .build();

        HttpResponse<String> overdueResponse = httpClient.send(overdueRequest, HttpResponse.BodyHandlers.ofString());

        assertThat(overdueResponse.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(overdueResponse.body()).contains("\"id\":" + overdueIssuedId);
        assertThat(overdueResponse.body()).contains("\"customerName\":\"Overdue Issued\"");
        assertThat(overdueResponse.body()).doesNotContain("\"id\":" + overduePaidId);
        assertThat(overdueResponse.body()).doesNotContain("\"id\":" + notOverdueIssuedId);
    }

    private String baseUrl(String path) {
        return "http://localhost:" + port + path;
    }

    private long createInvoice(String customerName, LocalDate issueDate) throws Exception {
        String createBody = String.format("""
                {
                    "customerName": "%s",
                    "amount": 100.00,
                    "issueDate": "%s"
                }
                """, customerName, issueDate);

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl("/api/invoices")))
                .header("Content-Type", APPLICATION_JSON.toString())
                .POST(HttpRequest.BodyPublishers.ofString(createBody))
                .build();

        HttpResponse<String> createResponse = httpClient.send(createRequest, HttpResponse.BodyHandlers.ofString());
        assertThat(createResponse.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        return extractId(createResponse.body());
    }

    private void updateInvoiceStatus(Long id, String customerName, LocalDate issueDate, String status) throws Exception {
        String updateBody = String.format("""
                {
                    "customerName": "%s",
                    "amount": 100.00,
                    "issueDate": "%s",
                    "status": "%s"
                }
                """, customerName, issueDate, status);

        HttpRequest updateRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl("/api/invoices/" + id)))
                .header("Content-Type", APPLICATION_JSON.toString())
                .PUT(HttpRequest.BodyPublishers.ofString(updateBody))
                .build();

        HttpResponse<String> updateResponse = httpClient.send(updateRequest, HttpResponse.BodyHandlers.ofString());
        assertThat(updateResponse.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    private long extractId(String responseBody) {
        Pattern idPattern = Pattern.compile("\"id\"\\s*:\\s*(\\d+)");
        Matcher matcher = idPattern.matcher(responseBody);
        assertThat(matcher.find()).as("id should be present in response").isTrue();
        return Long.parseLong(matcher.group(1));
    }
}
