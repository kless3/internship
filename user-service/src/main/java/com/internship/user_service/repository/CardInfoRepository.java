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

    boolean existsByNumber(String number);

    List<CardInfo> findByIdIn(List<Long> ids);

    List<CardInfo> findByUserId(Long userId);

    @Query("SELECT c FROM CardInfo c WHERE c.user.id IN :userIds")
    List<CardInfo> findByUserIds(@Param("userIds") List<Long> userIds);

    @Query("SELECT c FROM CardInfo c JOIN FETCH c.user WHERE c.id = :id")
    Optional<CardInfo> findByIdWithUser(@Param("id") Long id);

    @Query("SELECT c FROM CardInfo c JOIN FETCH c.user WHERE c.id IN :ids")
    List<CardInfo> findByIdInWithUser(@Param("ids") List<Long> ids);
}
