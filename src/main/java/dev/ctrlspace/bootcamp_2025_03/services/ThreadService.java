package dev.ctrlspace.bootcamp_2025_03.services;

import dev.ctrlspace.bootcamp_2025_03.exceptions.BootcampException;
import dev.ctrlspace.bootcamp_2025_03.model.Message;
import dev.ctrlspace.bootcamp_2025_03.model.Thread;
import dev.ctrlspace.bootcamp_2025_03.model.dto.MessageResponse;
import dev.ctrlspace.bootcamp_2025_03.model.dto.ThreadRequest;
import dev.ctrlspace.bootcamp_2025_03.model.dto.ThreadResponse;
import dev.ctrlspace.bootcamp_2025_03.repository.ThreadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ThreadService {

    @Value("${groq.model}")
    private String groqDefaultModel;

    private MessageService messageService;
    private ThreadRepository threadRepository;

    @Autowired
    public ThreadService(
            MessageService messageService,
            ThreadRepository threadRepository
    ) {
        this.messageService = messageService;
        this.threadRepository = threadRepository;
    }

    @Transactional
    public ThreadResponse createThread(ThreadRequest request) {
        Thread thread = new Thread();

        String model = (request.getCompletionModel() != null && !request.getCompletionModel().isBlank())
                ? request.getCompletionModel()
                : groqDefaultModel;
        thread.setCompletionModel(model);

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            thread.setTitle(request.getTitle());
        } else {
            thread.setTitle(null);
        }

        thread.setHasUnreadMessages(false);

        Thread saved = threadRepository.save(thread);

        return new ThreadResponse(
                saved.getId(),
                saved.getTitle(),
                saved.getCompletionModel(),
                false,
                new ArrayList<>()
        );
    }

    public List<ThreadResponse> getAllThreads() {
        List<Thread> threads = threadRepository.findAll();
        List<ThreadResponse> dtoList = new ArrayList<>();

        for (Thread thread : threads) {
            List<MessageResponse> messageDtos = new ArrayList<>();

            String threadTitle = thread.getTitle();
            if (threadTitle == null || threadTitle.isBlank()) {
                threadTitle = "Untitled Thread";
            }

            for (Message message : thread.getMessages()) {
                messageDtos.add(new MessageResponse(
                        message.getId(),
                        message.getContent(),
                        message.getIsCompletion(),
                        message.getCompletionModel(),
                        thread.getId(),
                        threadTitle
                ));
            }

            // Add the thread response to the final list
            dtoList.add(new ThreadResponse(
                    thread.getId(),
                    threadTitle,
                    thread.getCompletionModel(),
                    thread.isHasUnreadMessages(),
                    messageDtos
            ));
        }
        return dtoList;
    }

    public ThreadResponse getThreadById(Long id) throws BootcampException {
        Thread thread = threadRepository.findById(id)
                .orElseThrow(() -> new BootcampException(HttpStatus.NOT_FOUND, "Thread not found with id: " + id));

        List<MessageResponse> messages = messageService.getMessagesByThreadId(id);

        String threadTitle = (thread.getTitle() == null || thread.getTitle().isBlank())
                ? "Untitled Thread"
                : thread.getTitle();

        return new ThreadResponse(
                thread.getId(),
                threadTitle,
                thread.getCompletionModel(),
                thread.isHasUnreadMessages(),
                messages
        );
    }

    public Thread updateThread(Long id, Thread updatedThread) throws BootcampException {
        Thread existing = threadRepository.findById(id)
                .orElseThrow(() -> new BootcampException(HttpStatus.NOT_FOUND, "Thread not found with id: " + id));

        existing.setTitle(updatedThread.getTitle());
        return threadRepository.save(existing);
    }

    public void deleteThread(Long id) throws BootcampException {
        if (!threadRepository.existsById(id)) {
            throw new BootcampException(HttpStatus.NOT_FOUND, "Thread not found with id: " + id);
        }
        threadRepository.deleteById(id);
    }

    private ThreadResponse toDto(Thread thread) {
        List<MessageResponse> messageDtos = new ArrayList<>();

        String threadTitle = thread.getTitle();
        if (threadTitle == null || threadTitle.isBlank()) {
            threadTitle = "Untitled Thread";
        }

        for (Message message : thread.getMessages()) {
            messageDtos.add(new MessageResponse(
                    message.getId(),
                    message.getContent(),
                    message.getIsCompletion(),
                    message.getCompletionModel(),
                    thread.getId(),
                    threadTitle
            ));
        }

        return new ThreadResponse(
                thread.getId(),
                threadTitle,
                thread.getCompletionModel(),
                thread.isHasUnreadMessages(),
                messageDtos
        );
    }

    @Transactional
    public void markThreadAsRead(Long threadId) throws BootcampException {
        Thread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new BootcampException(HttpStatus.NOT_FOUND, "Thread not found with id: " + threadId));

        if (thread.isHasUnreadMessages()) {
            thread.setHasUnreadMessages(false);
            threadRepository.save(thread);
        }
    }
}
