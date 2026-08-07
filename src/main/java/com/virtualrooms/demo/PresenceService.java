package com.virtualrooms.demo;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PresenceService {

    private final SimpMessagingTemplate messagingTemplate;

    // roomId -> (sessionId -> username)   <-- antes era roomId -> Set<username>
    private final ConcurrentHashMap<String, Map<String, String>> rooms = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, Set<String>> sessionRooms = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> sessionUser = new ConcurrentHashMap<>();

    public PresenceService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleSubscribe(SessionSubscribeEvent event) {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();
        if (destination == null) return;

        String prefix = "/topic/sala/";
        if (!destination.startsWith(prefix)) return;

        String remainder = destination.substring(prefix.length());
        if (remainder.isEmpty()) return;

        String roomId;
        if (remainder.endsWith("/presenca")) {
            roomId = remainder.substring(0, remainder.length() - "/presenca".length());
        } else if (remainder.contains("/")) {
            return;
        } else {
            roomId = remainder;
        }

        String sessionId = accessor.getSessionId();

        String username = null;
        Principal principal = accessor.getUser();
        if (principal != null) {
            username = principal.getName();
        }
        if (username == null) {
            List<String> names = accessor.getNativeHeader("username");
            if (names != null && !names.isEmpty()) {
                username = names.get(0);
            }
        }
        if (username == null) {
            username = sessionId;
        }

        sessionUser.put(sessionId, username);
        sessionRooms.computeIfAbsent(sessionId, k -> ConcurrentHashMap.newKeySet()).add(roomId);
        rooms.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>()).put(sessionId, username);

        sendPresenceUpdate(roomId);
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        removeSession(accessor.getSessionId());
    }

    private void removeSession(String sessionId) {
        Set<String> joinedRooms = sessionRooms.remove(sessionId);
        sessionUser.remove(sessionId);
        if (joinedRooms == null) return;

        for (String roomId : joinedRooms) {
            Map<String, String> roomMap = rooms.get(roomId);
            if (roomMap != null) {
                roomMap.remove(sessionId);
                if (roomMap.isEmpty()) {
                    rooms.remove(roomId);
                }
            }
            sendPresenceUpdate(roomId);
        }
    }

    private void sendPresenceUpdate(String roomId) {
        Map<String, String> roomMap = rooms.get(roomId);
        List<String> snapshot = roomMap == null ? new ArrayList<>() : new ArrayList<>(roomMap.values());
        messagingTemplate.convertAndSend("/topic/sala/" + roomId + "/presenca", snapshot);
    }
}