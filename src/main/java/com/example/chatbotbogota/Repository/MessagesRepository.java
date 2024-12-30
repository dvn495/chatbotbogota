package com.example.chatbotbogota.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.chatbotbogota.Model.Messages;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface MessagesRepository extends JpaRepository<Messages, Long> {
    @Query("SELECT new map(m.id as messageId, m.user.id as userId, m.contenido as content, m.horaMensaje as messageTime) " +
            "FROM Messages m " +
            "WHERE m.horaMensaje BETWEEN :startOfDay AND :endOfDay")
    List<Map<String, Object>> findMessagesByDateRange(@Param("startOfDay") LocalDateTime startOfDay,
                                                      @Param("endOfDay") LocalDateTime endOfDay);


    @Query("SELECT new map(m.user.id as id, m.user.username as username, m.user.telefono as telefono) " +
            "FROM Messages m " +
            "WHERE m.horaMensaje BETWEEN :startOfDay AND :endOfDay")
    List<Map<String, Object>> findDistinctUserDetailsByDateRange(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

}
