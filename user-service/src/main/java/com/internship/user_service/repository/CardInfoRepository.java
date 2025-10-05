package com.internship.user_service.repository;

import com.internship.user_service.model.CardInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardInfoRepository extends JpaRepository<CardInfo, Long> {
    @Query(value = "SELECT * FROM card_info WHERE number = :number", nativeQuery = true)
    Optional<CardInfo> findByNumber(@Param("number") String number);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM CardInfo c WHERE c.number = :number")
    boolean existsByNumber(String number);

    List<CardInfo> findByIdIn(List<Long> ids);

    List<CardInfo> findByUserId(Long userId);

}
