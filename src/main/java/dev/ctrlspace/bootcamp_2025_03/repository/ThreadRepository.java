package dev.ctrlspace.bootcamp_2025_03.repository;

import dev.ctrlspace.bootcamp_2025_03.model.Thread;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ThreadRepository extends JpaRepository<Thread, Long> {
    boolean existsByIdAndUserId(Long id, Long userId);
    Optional<Thread> findByIdAndUserId(Long id, Long userId);
    List<Thread> findAllByUserId(Long userId);

//    @Query("SELECT t FROM Thread t LEFT JOIN FETCH t.messages WHERE t.id = :id")
//    Optional<Thread> findByIdWithMessages(@Param("id") Long id);
}
