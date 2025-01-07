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
        openIAService.clearConversation(session.getId());
        LOGGER.info("Cliente desconectado: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        long currentTime = System.currentTimeMillis();
        String sessionId = session.getId();
        long lastRequestTime = sessionLastRequestTime.getOrDefault(sessionId, 0L);

        if (currentTime - lastRequestTime < RATE_LIMIT_MS) {
            long waitTime = RATE_LIMIT_MS - (currentTime - lastRequestTime);

            try {
                LOGGER.info("Esperando {} ms antes de procesar el mensaje para la sesión {}", waitTime, sessionId);
                Thread.sleep(waitTime); // Esperar el tiempo restante
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.error("El hilo fue interrumpido durante la espera: {}", e.getMessage(), e);
                sendErrorMessage(session, "Interrupción inesperada durante la espera. Intenta nuevamente.");
                return; // Salir en caso de interrupción
            }
        }
        JSONObject jsonMessage = parseMessage(message.getPayload());
        try {
            if (!jsonMessage.has("message")) {
                sendErrorMessage(session, "El formato del mensaje no es válido. Debe ser un JSON con el campo 'message'.");
                return;
            }
        }catch (IllegalArgumentException e){
            sendErrorMessage(session, e.getMessage());
            return;
        }

        // Actualiza el tiempo de la última solicitud
        sessionLastRequestTime.put(sessionId, currentTime);
        String userMessage = jsonMessage.getString("message");

        boolean messageProcessed = false;
        int maxRetries = 3;
        int retries = 0;
        long retryDelayMs = 2000;

        while (!messageProcessed && retries < maxRetries){
            try {
                retries++;
                // Procesa el mensaje y envía la respuesta

                String aiResponse = openIAService.getCustomGPTResponse(userMessage, sessionId);
                sendMessage(session, aiResponse);
                messageProcessed=true;

            } catch (Exception e) {
                LOGGER.error("Error en el manejo del mensaje para la sesión {}: {}", sessionId, e.getMessage(), e);
                if (retries < maxRetries) {
                    try{
                        Thread.sleep(retryDelayMs);
                    } catch (InterruptedException ie){
                        Thread.currentThread().interrupt();
                        LOGGER.error("interrumpido durante el intento: {}", ie.getMessage(), ie);
                        break;
                    }
                } else {
                    LOGGER.error("Todos los intentos fallaron para la sesion {}.", sessionId);
                    sendErrorMessage(session, "Error interno del servidor. Por favor, intenta más tarde.");
                }
            }

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