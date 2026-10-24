package dev.ctrlspace.bootcamp_2025_03.repository;

import dev.ctrlspace.bootcamp_2025_03.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByThreadIdOrderByIdAsc(Long threadId);
    Optional<Message> findFirstByThreadIdAndUpdatedAtAfterAndIsCompletionTrueOrderByUpdatedAtAsc(
            Long threadId, Instant updatedAt);
}
