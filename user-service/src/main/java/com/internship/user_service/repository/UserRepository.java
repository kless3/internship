package com.internship.user_service.repository;

import com.internship.user_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query(value = "SELECT * FROM users WHERE email = :email", nativeQuery = true)
    Optional<User> findByEmail(@Param("email") String email);

    boolean existsByEmail(String email);

    List<User> findByIdIn(List<Long> ids);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.cards WHERE u.id = :id")
    Optional<User> findByIdWithCards(@Param("id") Long id);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.cards WHERE u.email = :email")
    Optional<User> findByEmailWithCards(@Param("email") String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.cards WHERE u.id IN :ids")
    List<User> findByIdInWithCards(@Param("ids") List<Long> ids);
}
