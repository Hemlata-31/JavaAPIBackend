package com.example.bfhl;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class BfhlApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(BfhlApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Application started, initiating webhook generation...");
        RestTemplate restTemplate = new RestTemplate();
        
        String generateUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
        
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", "John Doe");
        requestBody.put("regNo", "REG12347");
        requestBody.put("email", "john@example.com");
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        
        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);
        
        System.out.println("Sending POST to " + generateUrl);
        ResponseEntity<Map> response;
        try {
            response = restTemplate.postForEntity(generateUrl, request, Map.class);
        } catch (Exception e) {
            System.err.println("Error generating webhook: " + e.getMessage());
            return;
        }
        
        Map<String, Object> responseBody = response.getBody();
        if (responseBody != null) {
            System.out.println("Response received: " + responseBody);
            
            String accessToken = (String) responseBody.get("accessToken");
            String webhookUrl = (String) responseBody.get("webhook");
            
            String testWebhookUrl = webhookUrl != null ? webhookUrl : "https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA";
            
            String sqlQuery = "SELECT P.AMOUNT AS SALARY, CONCAT(E.FIRST_NAME, ' ', E.LAST_NAME) AS NAME, (EXTRACT(YEAR FROM CURRENT_DATE) - EXTRACT(YEAR FROM E.DOB)) AS AGE, D.DEPARTMENT_NAME FROM PAYMENTS P JOIN EMPLOYEE E ON P.EMP_ID = E.EMP_ID JOIN DEPARTMENT D ON E.DEPARTMENT = D.DEPARTMENT_ID WHERE EXTRACT(DAY FROM P.PAYMENT_TIME) <> 1 ORDER BY P.AMOUNT DESC LIMIT 1;";
            
            System.out.println("SQL Query generated: " + sqlQuery);
            
            Map<String, String> finalPayload = new HashMap<>();
            finalPayload.put("finalQuery", sqlQuery);
            
            HttpHeaders finalHeaders = new HttpHeaders();
            finalHeaders.set("Authorization", accessToken); // API expects just the token according to the prompt
            finalHeaders.set("Content-Type", "application/json");
            
            HttpEntity<Map<String, String>> finalRequest = new HttpEntity<>(finalPayload, finalHeaders);
            
            System.out.println("Sending solution to " + testWebhookUrl);
            try {
                ResponseEntity<String> finalResponse = restTemplate.postForEntity(testWebhookUrl, finalRequest, String.class);
                System.out.println("Final Submission Response Status: " + finalResponse.getStatusCode());
                System.out.println("Final Submission Response Body: " + finalResponse.getBody());
            } catch (Exception e) {
                System.err.println("Error testing webhook: " + e.getMessage());
            }
        } else {
            System.out.println("Failed to get response body from webhook generation.");
        }
    }
}
