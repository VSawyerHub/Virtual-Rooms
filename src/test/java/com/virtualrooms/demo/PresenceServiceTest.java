package com.virtualrooms.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PresenceServiceTest {
    private SimpMessagingTemplate messagingTemplate;
    private PresenceService presenceService;

    @BeforeEach
    void setUp() {
        messagingTemplate = mock(SimpMessagingTemplate.class);
        presenceService = new PresenceService(messagingTemplate);
    }

    private SessionSubscribeEvent buildSubscribeEvent(String sessionId, String destination, String username) {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.create(SimpMessageType.SUBSCRIBE);
        accessor.setSessionId(sessionId);
        accessor.setDestination(destination);
        if (username != null) {
            accessor.setNativeHeader("username", username);
        }
        accessor.setLeaveMutable(true);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        return new SessionSubscribeEvent(this, message);
    }

    private SessionDisconnectEvent buildDisconnectEvent(String sessionId) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
        accessor.setSessionId(sessionId);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        return new SessionDisconnectEvent(this, message, sessionId, CloseStatus.NORMAL);
    }

    @Test
    void duasSessoesComMesmoNome_apareceramSeparadasNaPresenca() {
        presenceService.handleSubscribe(buildSubscribeEvent("sessao-1", "/topic/sala/abc", "visitante"));
        presenceService.handleSubscribe(buildSubscribeEvent("sessao-2", "/topic/sala/abc", "visitante"));

        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
        verify(messagingTemplate, atLeastOnce())
                .convertAndSend(eq("/topic/sala/abc/presenca"), captor.capture());

        List<String> ultimaSnapshot = captor.getValue();
        assertEquals(2, ultimaSnapshot.size());
    }

    @Test
    void inscricaoNoTopicoDePresenca_naoDuplicaSufixoNoRoomId() {
        presenceService.handleSubscribe(buildSubscribeEvent("sessao-1", "/topic/sala/abc/presenca", "vic"));

        verify(messagingTemplate).convertAndSend(eq("/topic/sala/abc/presenca"), anyList());
        verify(messagingTemplate, never()).convertAndSend(eq("/topic/sala/abc/presenca/presenca"), anyList());
    }

    @Test
    void desconexao_removeApenasASessaoQueSaiu() {
        presenceService.handleSubscribe(buildSubscribeEvent("sessao-1", "/topic/sala/abc", "vic"));
        presenceService.handleSubscribe(buildSubscribeEvent("sessao-2", "/topic/sala/abc", "bab"));

        presenceService.handleDisconnect(buildDisconnectEvent("sessao-1"));

        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
        verify(messagingTemplate, atLeastOnce())
                .convertAndSend(eq("/topic/sala/abc/presenca"), captor.capture());

        assertEquals(List.of("bab"), captor.getValue());
    }
}
