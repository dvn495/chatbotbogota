package com.example.chatbotbogota.Service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import com.example.chatbotbogota.Model.User;
import com.example.chatbotbogota.Repository.UserRepository;

import java.util.Optional;

@AllArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    public Optional<User> getByUsername(String username){
        return userRepository.findByUsername(username);
    }

    public Optional<User> getByPhone(Long telefono){
        return userRepository.findByTelefono(telefono);
    }

}
