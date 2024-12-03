package com.example.chatbotbogota.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.chatbotbogota.Model.Messages;
import com.example.chatbotbogota.Model.User;
import com.example.chatbotbogota.Repository.MessagesRepository;
import com.example.chatbotbogota.Repository.UserRepository;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MessagesService {

    @Autowired
    private MessagesRepository messagesRepository;
    @Autowired
    private UserRepository userRepository;

    public void saveQuestionUser(String preguntaUsuario, Integer userId){
        Messages messages = new Messages();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User don't found"));

        messages.setContenido(preguntaUsuario);
        messages.setHoraMensaje(LocalDateTime.of(LocalDate.now(), LocalTime.now()));
        messages.setUser(user);
        messagesRepository.save(messages);
    }

    public List<Map<String, Object>> getMessagesByDateRange(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        return messagesRepository.findMessagesByDateRange(startOfDay, endOfDay);
    }


    public List<Map<String, Object>> getUsersWhoWroteToday(){
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        return messagesRepository.findDistinctUserDetailsByDateRange(startOfDay, endOfDay);

    }



}
