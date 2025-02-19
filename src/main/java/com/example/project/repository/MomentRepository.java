package com.example.project.repository;


import com.example.project.model.Moment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MomentRepository extends JpaRepository<Moment, Long> {

    Optional<Moment> findByUsername(String username); // Поиск пользователя по имени пользователя
}
