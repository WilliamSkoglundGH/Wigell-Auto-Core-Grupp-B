package com.wac.autocore.repository;

import com.wac.autocore.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT DISTINCT b FROM Booking b LEFT JOIN FETCH b.serviceItems item LEFT JOIN FETCH item.serviceItem")
    List<Booking> findAllWithServiceItems();

    @Query("SELECT DISTINCT b FROM Booking b " +
            "LEFT JOIN FETCH b.serviceItems item " +
            "LEFT JOIN FETCH item.serviceItem " +
            "LEFT JOIN FETCH b.vehicle " +
            "WHERE b.id = :id")
    Optional<Booking> findByIdWithDetails(@Param("id") Long id);
}

