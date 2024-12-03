package com.example.chatbotbogota.Config;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.example.chatbotbogota.Service.JwtService;

import java.util.Map;

public class JwtHandshakeInterceptor implements HandshakeInterceptor {
    private final JwtService jwtService;

    public JwtHandshakeInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        String query = request.getURI().getQuery();
        if (query != null && query.startsWith("token=")) {
            String token = query.split("token=")[1];
            if (jwtService.isTokenValid(token)) {
                // Token válido, continuar con la conexión
                return true;
            }
        }
        response.setStatusCode(HttpStatus.UNAUTHORIZED); // Rechaza la conexión si el token no es válido
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        // Opcional: lógica después del handshake
    }
}
