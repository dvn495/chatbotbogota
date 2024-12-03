package com.example.chatbotbogota.Config;

public class LoginRequest {
    private String username;
    private Long telefono;
    private String role;

    // Constructor vacío
    public LoginRequest() {
    }

    // Constructor con parámetros
    public LoginRequest(String username, Long telefono, String role) {
        this.username = username;
        this.telefono = telefono;
        this.role = role;
    }

    // Getter y Setter
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getTelefono() {
        return telefono;
    }

    public void setTelefono(Long telefono) {
        this.telefono = telefono;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
