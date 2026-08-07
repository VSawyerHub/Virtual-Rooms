package com.virtualrooms.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class RoomCleanupServiceTest {

    private RoomRepository roomRepository;

    @BeforeEach
    void setUp() {
        roomRepository = mock(RoomRepository.class);
    }

    @Test
    void cleanupOldRooms_removeSalasInativasAntesDoCutoff() {
        RoomCleanupService service = new RoomCleanupService(roomRepository, 30);

        Room salaAntiga = new Room();
        salaAntiga.setId("antiga");
        when(roomRepository.findByLastActivityAtBefore(any())).thenReturn(List.of(salaAntiga));

        service.cleanupOldRooms();

        verify(roomRepository).deleteAll(List.of(salaAntiga));
    }

    @Test
    void cleanupOldRooms_semSalasAntigas_naoChamaDeleteAll() {
        RoomCleanupService service = new RoomCleanupService(roomRepository, 30);
        when(roomRepository.findByLastActivityAtBefore(any())).thenReturn(Collections.emptyList());

        service.cleanupOldRooms();

        verify(roomRepository, never()).deleteAll(anyList());
    }

    @Test
    void cleanupOldRooms_calculaCutoffComBaseNoThresholdConfigurado() {
        RoomCleanupService service = new RoomCleanupService(roomRepository, 15);
        when(roomRepository.findByLastActivityAtBefore(any())).thenReturn(Collections.emptyList());

        LocalDateTime cutoffEsperado = LocalDateTime.now().minusMinutes(15);
        service.cleanupOldRooms();

        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(roomRepository).findByLastActivityAtBefore(captor.capture());
        LocalDateTime cutoffUsado = captor.getValue();

        long diffSegundos = Math.abs(Duration.between(cutoffEsperado, cutoffUsado).getSeconds());
        assertTrue(diffSegundos < 2, "cutoff deveria ser ~15 minutos atrás do momento da chamada");
    }
}