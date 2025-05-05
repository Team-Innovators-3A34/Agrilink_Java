package org.example.demo.services.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.demo.models.User;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class MatchService {

    private final userService userService;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public MatchService(userService userService) {
        this.userService = userService;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public List<User> getMatchedUsers(int currentUserId) {
        List<User> allUsers = userService.getAllUsers();

        // Remove current user from the list if necessary

        List<Map<String, Object>> userData = new ArrayList<>();
        for (User user : allUsers) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("description", user.getDescription());

            try {
                map.put("latitude", String.valueOf(user.getLatitude())); // Convert float to String
                map.put("longitude", String.valueOf(user.getLongitude())); // Convert float to String // Convert to double
                map.put("score", String.valueOf(user.getScore())); // Convert int to String
            } catch (NumberFormatException e) {
                e.printStackTrace();
                continue; // Skip this user if there's a problem with data format
            }

            userData.add(map);
        }

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("user_id", currentUserId);
            requestBody.put("users", userData);

            String requestJson = objectMapper.writeValueAsString(requestBody);
            System.out.println("Request JSON: " + requestJson);  // Print JSON for debugging

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://127.0.0.1:5000/match"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Response: " + response.body());  // Print response for debugging

            if (response.statusCode() != 200) {
                return Collections.emptyList();
            }

            ObjectNode responseJson = (ObjectNode) objectMapper.readTree(response.body());
            if (!responseJson.has("matches")) {
                return Collections.emptyList();
            }

            List<Integer> matchedIds = objectMapper.convertValue(
                    responseJson.get("matches"), objectMapper.getTypeFactory().constructCollectionType(List.class, Integer.class));

            // Filter and return matched users
            return allUsers.stream()
                    .filter(user -> matchedIds.contains(user.getId()))
                    .toList();

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }



    }
}

