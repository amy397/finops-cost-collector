package com.finops.collector.repository;

import com.finops.collector.entity.ServiceCost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.time.LocalDate;
import java.util.List;

public interface ServiceCostRepository extends JpaRepository<ServiceCost, Long> {

    List<ServiceCost> findByCostDate(LocalDate costDate);

    @Modifying
    void deleteByCostDate(LocalDate costDate);
}
