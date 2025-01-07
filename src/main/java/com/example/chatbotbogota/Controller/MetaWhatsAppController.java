package com.example.chatbotbogota.Controller;

import com.example.chatbotbogota.Service.OpenIAService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/whatsapp")
public class MetaWhatsAppController {
    private static final Logger logger = LoggerFactory.getLogger(MetaWhatsAppController.class);
    private final OpenIAService openIAService;

    // Meta WhatsApp API configuration constants
    private static final String META_API_VERSION = "v21.0";
    private static final String PHONE_NUMBER_ID = "489757120892951";
    private static final String ACCESS_TOKEN = "EAASXn0lKkTABO9J6YqS9XJ7ntYBUDYz28BPTBCMLm7O4z3t4bOjPgqe1UKBkx1iFVXLwo2Ef6NC6gUUTWFSXALT9xDEB5xZBUbZAduS0Jfo83Au8YHEQhHloxxalVvjvsUDURprkiQajOwVGub1rKK6dY6GsG9X2NQ5T1Y8XPL6zYsPuYTHOQiF23HkFZCUdZAK9wADDsPsei50SXy21CkZAXKg6ToyGPDkyhm2CsmM7lnLFudCwZD";
    private static final String VERIFY_TOKEN = "eancampuslands2024";

    // Constructing the Meta API URL
    private final String META_API_URL = "https://graph.facebook.com/" + META_API_VERSION + "/" + PHONE_NUMBER_ID + "/messages";

    public MetaWhatsAppController(OpenIAService openIAService) {
        this.openIAService = openIAService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(@RequestBody String requestBody) {
        try {
            // Parse incoming webhook payload
            ObjectMapper mapper = new ObjectMapper();
            JsonNode payload = mapper.readTree(requestBody);

            // Handle Meta's webhook verification challenge
            if (payload.has("hub.challenge")) {
                return ResponseEntity.ok(payload.get("hub.challenge").asText());
            }

            // Process incoming WhatsApp messages
            if (payload.has("entry")) {
                JsonNode entry = payload.get("entry").get(0);
                if (entry.has("changes")) {
                    JsonNode changes = entry.get("changes").get(0);
                    if (changes.has("value") && changes.get("value").has("messages")) {
                        JsonNode message = changes.get("value").get("messages").get(0);

                        // Extract user's message and phone number
                        String from = message.get("from").asText();
                        String messageText = message.get("text").get("body").asText();

                        logger.info("Mensaje recibido de WhatsApp: [From: {}, Message: {}]", from, messageText);

                        // Get AI response using existing OpenIAService
                        String aiResponse = openIAService.getCustomGPTResponse(messageText, from);
                        logger.info("Respuesta del AI: {}", aiResponse);

                        // Send response back through WhatsApp
                        sendWhatsAppMessage(from, aiResponse);
                        logger.info("Respuesta enviada a WhatsApp: [To: {}, Message: {}]", from, aiResponse);
                    }
                }
            }

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            logger.error("Error procesando webhook de WhatsApp: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private void sendWhatsAppMessage(String to, String message) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // Prepare headers with authentication
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(ACCESS_TOKEN);

            // Construct WhatsApp message payload
            JSONObject messageData = new JSONObject();
            messageData.put("messaging_product", "whatsapp");
            messageData.put("to", to);
            messageData.put("type", "text");

            JSONObject text = new JSONObject();
            text.put("preview_url", false);
            text.put("body", message);
            messageData.put("text", text);

            // Create and send HTTP request
            HttpEntity<String> request = new HttpEntity<>(messageData.toString(), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(META_API_URL, request, String.class);

            // Handle unsuccessful responses
            if (!response.getStatusCode().is2xxSuccessful()) {
                logger.error("Error enviando mensaje de WhatsApp: {}", response.getBody());
                throw new RuntimeException("Error enviando mensaje de WhatsApp");
            }

        } catch (Exception e) {
            logger.error("Error en el envío del mensaje de WhatsApp: ", e);
            throw new RuntimeException("Error en el envío del mensaje de WhatsApp", e);
        }
    }

    @GetMapping("/webhook")
    public ResponseEntity<?> verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {

        // Verify webhook according to Meta's requirements
        if (mode != null && token != null) {
            if (mode.equals("subscribe") && token.equals(VERIFY_TOKEN)) {
                logger.info("Webhook verificado exitosamente");
                return ResponseEntity.ok(challenge);
            }
        }
        return ResponseEntity.badRequest().build();
    }
}
