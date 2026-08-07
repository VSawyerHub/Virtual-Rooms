package com.virtualrooms.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, String> {
    List<Room> findByLastActivityAtBefore(LocalDateTime cutoff);
}
