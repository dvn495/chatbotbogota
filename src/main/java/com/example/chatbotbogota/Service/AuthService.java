package com.example.chatbotbogota.Service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import com.example.chatbotbogota.Config.AuthResponse;
import com.example.chatbotbogota.Config.LoginRequest;
import com.example.chatbotbogota.Config.RegisterRequest;
import com.example.chatbotbogota.Model.Role;
import com.example.chatbotbogota.Model.User;
import com.example.chatbotbogota.Repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalTime;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    // Constructor manual para inyección de dependencias
    @Autowired
    public AuthService(UserRepository userRepository, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public AuthResponse login(LoginRequest request) {
        // Autenticación sin contraseña
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Verifica si el teléfono proporcionado coincide con el del usuario
        if (!user.getTelefono().equals(request.getTelefono())) {
            throw new RuntimeException("Teléfono incorrecto");
        }

        // Genera un token para el usuario autenticado
        String token = jwtService.getToken(user);

        // Crear AuthResponse sin builder
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        return response;
    }

    public AuthResponse register(RegisterRequest request) {
        // Crea un nuevo usuario y lo guarda
        User user = new User();
        user.setUsername(request.getUsername());
        user.setTelefono(request.getTelefono());
        user.setCreation_time(LocalTime.now());
        user.setRole(Role.USER);

        userRepository.save(user);

        // Genera un token para el nuevo usuario registrado
        AuthResponse response = new AuthResponse();
        response.setToken(jwtService.getToken(user));
        return response;
    }
}
