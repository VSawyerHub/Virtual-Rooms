package com.virtualrooms.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RoomCleanupService {
    private static final Logger log = LoggerFactory.getLogger(RoomCleanupService.class);

    private final RoomRepository roomRepository;

    // cleanup threshold in minutes (configurable via application.properties: rooms.cleanup.minutes)
    private final long cleanupThresholdMinutes;

    public RoomCleanupService(RoomRepository roomRepository,
                              @Value("${rooms.cleanup.minutes:30}") long cleanupThresholdMinutes) {
        this.roomRepository = roomRepository;
        this.cleanupThresholdMinutes = cleanupThresholdMinutes;
    }

    @Scheduled(fixedRate = 60000)
    public void cleanupOldRooms() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(cleanupThresholdMinutes);
        List<Room> oldRooms = roomRepository.findByLastActivityAtBefore(cutoff);
        if (oldRooms == null || oldRooms.isEmpty()) {
            return;
        }
        log.info("Cleaning up {} rooms last active before {}", oldRooms.size(), cutoff);
        roomRepository.deleteAll(oldRooms);
    }
}
