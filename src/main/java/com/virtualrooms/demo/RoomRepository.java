package com.virtualrooms.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, String> {
    List<Room> findByLastActivityAtBefore(LocalDateTime cutoff);

    Optional<Room> findByCode(String code);
}
