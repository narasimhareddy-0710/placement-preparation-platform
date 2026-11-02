package com.placement.placement_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class InterviewFeedbackService {
    private final String apiKey;
    private final String model;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    public InterviewFeedbackService(@Value("${app.openai.api-key:}") String apiKey,
                                    @Value("${app.openai.model:gpt-4.1-mini}") String model) {
        this.apiKey = apiKey;
        this.model = model;
    }

    public String review(String role, String question, String answer) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "AI feedback is not configured. Add OPENAI_API_KEY to the server environment.");
        }
        try {
            String instructions = "You are a supportive placement interview coach for college students. Review only the answer provided. Return a score out of 10, two specific strengths, two actionable improvements, and one concise example closing sentence. Consider relevance, structure, evidence, ownership, and clarity. Do not invent student experience. Use plain language and keep the response under 180 words.";
            String input = "Interview round: " + role + "\nQuestion: " + question + "\nStudent answer: " + answer;
            String body = "{\"model\":" + json(model) + ",\"instructions\":" + json(instructions)
                    + ",\"input\":" + json(input) + "}";
            HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.openai.com/v1/responses"))
                    .timeout(Duration.ofSeconds(35))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "The AI feedback provider returned an error. Check the server key and model.");
            }
            StringBuilder result = new StringBuilder();
            Matcher textMatches = Pattern.compile("\\\"type\\\"\\s*:\\s*\\\"output_text\\\"\\s*,\\s*\\\"text\\\"\\s*:\\s*\\\"((?:\\\\\\\\.|[^\\\"\\\\\\\\])*)\\\"", Pattern.DOTALL)
                    .matcher(response.body());
            while (textMatches.find()) {
                if (!result.isEmpty()) result.append("\n");
                result.append(unescape(textMatches.group(1)));
            }
            if (result.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "The AI returned no feedback.");
            return result.toString();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "AI feedback was interrupted.");
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Could not retrieve AI feedback.");
        }
    }

    private String json(String value) {
        StringBuilder out = new StringBuilder("\"");
        for (char c : value.toCharArray()) {
            switch (c) {
                case '\\' -> out.append("\\\\");
                case '"' -> out.append("\\\"");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> { if (c < 0x20) out.append(String.format("\\u%04x", (int)c)); else out.append(c); }
            }
        }
        return out.append('"').toString();
    }

    private String unescape(String value) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c != '\\' || i + 1 >= value.length()) { out.append(c); continue; }
            char e = value.charAt(++i);
            switch (e) {
                case '"' -> out.append('"');
                case '\\' -> out.append('\\');
                case '/' -> out.append('/');
                case 'b' -> out.append('\b');
                case 'f' -> out.append('\f');
                case 'n' -> out.append('\n');
                case 'r' -> out.append('\r');
                case 't' -> out.append('\t');
                case 'u' -> { if (i + 4 < value.length()) { out.append((char)Integer.parseInt(value.substring(i + 1, i + 5), 16)); i += 4; } }
                default -> out.append(e);
            }
        }
        return out.toString();
    }
}
