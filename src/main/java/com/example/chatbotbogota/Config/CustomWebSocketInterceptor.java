package com.example.chatbotbogota.Config;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import com.example.chatbotbogota.Service.JwtService;

@Component
public class CustomWebSocketInterceptor extends HttpSessionHandshakeInterceptor {

    private final JwtService jwtService;

    public CustomWebSocketInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        String query = request.getURI().getQuery();
        if (query != null && query.contains("token=")) {
            String token = query.split("token=")[1];
            // Validación del token JWT
            if (isValidToken(token)) {
                return true;
            } else {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }
        }
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return false;
    }

    private boolean isValidToken(String token) {
        // Lógica de validación del token usando JwtService
        return jwtService.isTokenValid(token);
    }
}

