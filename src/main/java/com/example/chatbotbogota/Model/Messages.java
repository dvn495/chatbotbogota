package com.example.chatbotbogota.Model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Data
@Table(name="mensajes")
public class Messages {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name ="contenido", nullable = false)
    private String contenido;

    @Column(name = "hora_mensaje", nullable = false)
    private LocalDateTime horaMensaje;

    @ManyToOne
    @JoinColumn(name="user_id", nullable = false)
    private User user;
}
