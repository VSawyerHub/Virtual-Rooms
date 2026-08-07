package com.virtualrooms.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomRepository roomRepository;

    public RoomController(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @PostMapping
    public ResponseEntity<String> createRoom() {
        Room room = new Room();
        Room saved = roomRepository.save(room);
        return ResponseEntity.ok(saved.getId());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Void> checkRoom(@PathVariable("id") String id) {
        return roomRepository.findById(id)
                .map(r -> ResponseEntity.ok().<Void>build())
                .orElse(ResponseEntity.notFound().build());
    }
}
