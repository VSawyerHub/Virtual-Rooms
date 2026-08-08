package com.virtualrooms.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomRepository roomRepository;
    private final PresenceService presenceService;

    public RoomController(RoomRepository roomRepository, PresenceService presenceService) {
        this.roomRepository = roomRepository;
        this.presenceService = presenceService;
    }

    @PostMapping
    public ResponseEntity<String> createRoom(@RequestBody(required = false) CreateRoomRequest request) {
        Room room = new Room();
        if (request != null) {
            room.setTag(request.getTag());
            room.setDescription(request.getDescription());
            room.setTitle(request.getTitle());
        }
        Room saved = roomRepository.save(room);
        return ResponseEntity.ok(saved.getCode());
    }

    @GetMapping("/{code}")
    public ResponseEntity<Room> checkRoom(@PathVariable String code) {
        return roomRepository.findByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{code}")
    public ResponseEntity<Room> updateRoom(@PathVariable String code, @RequestBody CreateRoomRequest request) {
        return roomRepository.findByCode(code).map(room -> {
            room.setTitle(request.getTitle());
            room.setTag(request.getTag());
            room.setDescription(request.getDescription());
            return ResponseEntity.ok(roomRepository.save(room));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<RoomSummary> listActiveRooms() {
        return roomRepository.findAll().stream()
                .map(room -> new RoomSummary(
                        room.getCode(), room.getTitle(), room.getTag(),
                        room.getDescription(), presenceService.countUsers(room.getCode())
                ))
                .toList();
    }

    @GetMapping("/{code}/presence")
    public List<String> getPresence(@PathVariable String code) {
        return presenceService.getUsers(code);
    }

    public static class CreateRoomRequest {
        private String tag;
        private String description;
        private String title;

        public String getTag() {return tag;}

        public void setTag(String tag) {this.tag = tag;}

        public String getDescription() {return description;}

        public void setDescription(String description) {this.description = description;}

        public String getTitle() {return title;}

        public void setTitle(String title) {this.title = title;}
    }
}

