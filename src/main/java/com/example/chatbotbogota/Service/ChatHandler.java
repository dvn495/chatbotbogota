package com.example.chatbotbogota.Service;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Service
public class ChatHandler extends TextWebSocketHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatHandler.class);
    private static final long RATE_LIMIT_MS = TimeUnit.SECONDS.toMillis(5);

    private final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final Map<String, Long> sessionLastRequestTime = new ConcurrentHashMap<>();
    private final OpenIAService openIAService;
    @Autowired
    private MessagesService messagesService;
    @Autowired
    public ChatHandler(OpenIAService openIAService) {
        this.openIAService = openIAService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        LOGGER.info("Cliente conectado: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        sessionLastRequestTime.remove(session.getId());
        LOGGER.info("Cliente desconectado: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        long currentTime = System.currentTimeMillis();
        String sessionId = session.getId();
        long lastRequestTime = sessionLastRequestTime.getOrDefault(sessionId, 0L);

        try {
            if (currentTime - lastRequestTime < RATE_LIMIT_MS) {
                sendErrorMessage(session, "Por favor, espera unos segundos antes de enviar otro mensaje.");
                return;
            }

            JSONObject jsonMessage = parseMessage(message.getPayload());
            if (!jsonMessage.has("message")) {
                sendErrorMessage(session, "El formato del mensaje no es válido. Debe ser un JSON con el campo 'message'.");
                return;
            }

            // Actualiza el tiempo de la última solicitud
            sessionLastRequestTime.put(sessionId, currentTime);

            // Procesa el mensaje y envía la respuesta
            String userMessage = jsonMessage.getString("message");
            String aiResponse = openIAService.getCustomGPTResponse(userMessage);
            sendMessage(session, aiResponse);

        } catch (Exception e) {
            LOGGER.error("Error en el manejo del mensaje para la sesión {}: {}", sessionId, e.getMessage(), e);
            sendErrorMessage(session, "Error interno del servidor. Por favor, intenta más tarde.");
        }
    }

    private JSONObject parseMessage(String payload) {
        try {
            return new JSONObject(payload);
        } catch (Exception e) {
            LOGGER.error("Error al parsear el mensaje JSON: {}", e.getMessage());
            throw new IllegalArgumentException("Formato de mensaje inválido");
        }
    }

    private void sendMessage(WebSocketSession session, String message) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(message));
            }
        } catch (IOException e) {
            LOGGER.error("Error al enviar mensaje a la sesión {}: {}", session.getId(), e.getMessage(), e);
        }
    }

    private void sendErrorMessage(WebSocketSession session, String errorMessage) {
        try {
            if (session.isOpen()) {
                JSONObject error = new JSONObject();
                error.put("error", true);
                error.put("message", errorMessage);
                session.sendMessage(new TextMessage(error.toString()));
            }
        } catch (IOException e) {
            LOGGER.error("Error al enviar mensaje de error a la sesión {}: {}", session.getId(), e.getMessage(), e);
        }
    }
}