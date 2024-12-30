package com.example.chatbotbogota.Controller;

import com.example.chatbotbogota.Service.JwtService;
import com.example.chatbotbogota.Service.OpenIAService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.chatbotbogota.Model.User;
import com.example.chatbotbogota.Repository.UserRepository;

import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final OpenIAService openIAService;

    @Autowired
    public UserController(UserRepository userRepository, JwtService jwtService, OpenIAService openIAService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.openIAService = openIAService;
    }

    @GetMapping("/phone")
    public ResponseEntity<User> findByPhone(@RequestParam Long telefono) {
        return userRepository.findByTelefono(telefono)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/username")
    public ResponseEntity<String> getUsername(@RequestHeader("Authorization") String token) {

        // Validar y procesar el token
        token = token.replace("Bearer", "").trim();

        if (!jwtService.isTokenValid(token)) {
            return ResponseEntity.status(401).body("Invalid Token or Expired");
        }

        String username = jwtService.getUsernameFromToken(token);

        openIAService.setUserName(username);

        return ResponseEntity.ok("Message successfully saved");
    }
}
