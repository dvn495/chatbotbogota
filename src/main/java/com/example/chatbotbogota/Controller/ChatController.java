package com.example.chatbotbogota.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class ChatController {

    @GetMapping("/")
    public String showChatPage() {
        return "chatBogota";
    }

}

