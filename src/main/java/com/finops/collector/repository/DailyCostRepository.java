package com.finops.collector.repository;

import com.finops.collector.entity.DailyCost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyCostRepository extends JpaRepository<DailyCost, Long> {

    Optional<DailyCost> findByCostDate(LocalDate costDate);

    boolean existsByCostDate(LocalDate costDate);
}
