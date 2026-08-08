package com.virtualrooms.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoomController.class)
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoomRepository roomRepository;

    @MockitoBean
    private PresenceService presenceService;

    @Test
    void createRoom_devolveCodigoDaSalaSalva() throws Exception {
        Room saved = new Room();
        saved.setId("sala-123");
        saved.setCode("ABC123");
        saved.setCreatedAt(LocalDateTime.now());
        when(roomRepository.save(any(Room.class))).thenReturn(saved);

        mockMvc.perform(post("/api/rooms"))
                .andExpect(status().isOk())
                .andExpect(content().string("ABC123"));
    }

    @Test
    void checkRoom_salaExistente_devolve200_semExporIdInterno() throws Exception {
        Room room = new Room();
        room.setId("sala-existente");
        room.setCode("ABC123");
        when(roomRepository.findByCode("ABC123")).thenReturn(Optional.of(room));

        mockMvc.perform(get("/api/rooms/ABC123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ABC123"))
                .andExpect(jsonPath("$.id").doesNotExist());
    }

    @Test
    void checkRoom_salaInexistente_devolve404() throws Exception {
        when(roomRepository.findByCode("NAOEXISTE")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/rooms/NAOEXISTE"))
                .andExpect(status().isNotFound());
    }
}
