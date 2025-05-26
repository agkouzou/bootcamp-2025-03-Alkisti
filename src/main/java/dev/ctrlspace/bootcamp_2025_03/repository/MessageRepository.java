package dev.ctrlspace.bootcamp_2025_03.repository;

import dev.ctrlspace.bootcamp_2025_03.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByThreadIdOrderByIdAsc(Long threadId);
}
