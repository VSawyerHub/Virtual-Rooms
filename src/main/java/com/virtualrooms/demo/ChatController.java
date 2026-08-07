package com.virtualrooms.demo;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/sala/{roomId}/enviar")
    public void sendToRoom(@DestinationVariable("roomId") String roomId, ChatMessage message) {
        if (message.getSentAt() == null) {
            message.setSentAt(LocalDateTime.now());
        }
        messagingTemplate.convertAndSend("/topic/sala/" + roomId, message);
    }
}
