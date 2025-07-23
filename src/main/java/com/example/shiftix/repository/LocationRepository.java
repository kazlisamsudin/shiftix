package com.example.shiftix.repository;

import com.example.shiftix.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findByActiveTrue();
    List<Location> findByNameContainingIgnoreCase(String name);
}
