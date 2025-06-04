package dev.ctrlspace.bootcamp_2025_03.services;

import dev.ctrlspace.bootcamp_2025_03.exceptions.BootcampException;
import dev.ctrlspace.bootcamp_2025_03.model.Message;
import dev.ctrlspace.bootcamp_2025_03.model.Thread;
import dev.ctrlspace.bootcamp_2025_03.model.User;
import dev.ctrlspace.bootcamp_2025_03.model.dto.MessageResponse;
import dev.ctrlspace.bootcamp_2025_03.model.dto.ThreadRequest;
import dev.ctrlspace.bootcamp_2025_03.model.dto.ThreadResponse;
import dev.ctrlspace.bootcamp_2025_03.repository.ThreadRepository;
import dev.ctrlspace.bootcamp_2025_03.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class ThreadService {
    @Value("${groq.model}")
    private String groqDefaultModel;

    private MessageService messageService;
    private ThreadRepository threadRepository;
    private UserRepository userRepository;

    @Autowired
    public ThreadService(
            MessageService messageService,
            ThreadRepository threadRepository,
            UserRepository userRepository
    ) {
        this.messageService = messageService;
        this.threadRepository = threadRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser(Authentication authentication) throws BootcampException {
        try {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String userIdStr = jwt.getClaim("sub");
            Long userId = Long.parseLong(userIdStr);
            return userRepository.findById(userId)
                    .orElseThrow(() -> new BootcampException("User not found with id: " + userId));
        } catch (NumberFormatException e) {
            throw new BootcampException("Invalid user ID in token: " + e.getMessage());
        } catch (ClassCastException e) {
            throw new BootcampException("Authentication principal is not a valid JWT token.");
        }
    }

    @Transactional
    public ThreadResponse createThread(ThreadRequest request, Authentication authentication) throws BootcampException {
        User currentUser = getCurrentUser(authentication);

        Thread thread = new Thread();

        String model = (request.getCompletionModel() != null && !request.getCompletionModel().isBlank())
                ? request.getCompletionModel()
                : groqDefaultModel;

        model = model.toLowerCase();

        thread.setCompletionModel(model);

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            thread.setTitle(request.getTitle());
        } else {
            thread.setTitle(null);
        }

        thread.setHasUnreadMessages(false);
        thread.setUser(currentUser);

        Instant now = Instant.now();
        thread.setCreatedAt(now);
        thread.setUpdatedAt(now);

        Thread saved = threadRepository.save(thread);

        return toDto(saved);
//        return new ThreadResponse(
//                saved.getId(),
//                saved.getTitle(),
//                saved.getCompletionModel(),
//                false,
//                new ArrayList<>()
//        );
    }

    public List<ThreadResponse> getAllThreads(Authentication authentication) throws BootcampException {
        User currentUser = getCurrentUser(authentication);
        Long userId = currentUser.getId();

        List<Thread> threads = threadRepository.findAllByUserId(userId);  // filter by user id
        List<ThreadResponse> dtoList = new ArrayList<>();

        for (Thread thread : threads) {
            dtoList.add(toDto(thread));
//            List<MessageResponse> messageDtos = new ArrayList<>();
//
//            String threadTitle = thread.getTitle();
//            if (threadTitle == null || threadTitle.isBlank()) {
//                threadTitle = "Untitled Thread";
//            }
//
//            for (Message message : thread.getMessages()) {
//                messageDtos.add(new MessageResponse(
//                        message.getId(),
//                        message.getContent(),
//                        message.getIsCompletion(),
//                        message.getCompletionModel(),
//                        thread.getId()
//                ));
//            }
//
//            // Add the thread response to the final list
//            dtoList.add(new ThreadResponse(
//                    thread.getId(),
//                    threadTitle,
//                    thread.getCompletionModel(),
//                    thread.isHasUnreadMessages(),
//                    messageDtos
//            ));
        }
        return dtoList;
    }

    public ThreadResponse getThreadById(Long id, Authentication authentication) throws BootcampException {
        User currentUser = getCurrentUser(authentication);
        Long userId = currentUser.getId();

        Thread thread = threadRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BootcampException(HttpStatus.NOT_FOUND, "Thread not found or not owned by user"));

//        List<MessageResponse> messages = messageService.getMessagesByThreadId(id, authentication);

//        String threadTitle = (thread.getTitle() == null || thread.getTitle().isBlank())
//                ? "Untitled Thread"
//                : thread.getTitle();

//        return new ThreadResponse(
//                thread.getId(),
//                threadTitle,
//                thread.getCompletionModel(),
//                thread.isHasUnreadMessages(),
//                messages
//        );
        return toDto(thread);
    }

    public Thread updateThread(Long id, Thread updatedThread, Authentication authentication) throws BootcampException {
        User currentUser = getCurrentUser(authentication);
        Long userId = currentUser.getId();

        Thread existing = threadRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BootcampException(HttpStatus.NOT_FOUND, "Thread not found or not owned by user"));

        existing.setTitle(updatedThread.getTitle());
        existing.setUpdatedAt(Instant.now());

        return threadRepository.save(existing);
    }

    public void deleteThread(Long id, Authentication authentication) throws BootcampException {
        User currentUser = getCurrentUser(authentication);
        Long userId = currentUser.getId();

        Thread thread = threadRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BootcampException(HttpStatus.NOT_FOUND, "Thread not found or not owned by user"));

        threadRepository.delete(thread);
    }

    private MessageResponse toDto(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getContent(),
                message.getIsCompletion(),
                message.getCompletionModel(),
                message.getThread().getId(),
                message.getCreatedAt(),
                message.getUpdatedAt()
        );
    }

    private ThreadResponse toDto(Thread thread) {
        List<MessageResponse> messageDtos = new ArrayList<>();
        for (Message message : thread.getMessages()) {
            messageDtos.add(toDto(message));
        }

        String threadTitle = thread.getTitle();
        if (threadTitle == null || threadTitle.isBlank()) {
            threadTitle = "Untitled Thread";
        }

//        for (Message message : thread.getMessages()) {
//            messageDtos.add(new MessageResponse(
//                    message.getId(),
//                    message.getContent(),
//                    message.getIsCompletion(),
//                    message.getCompletionModel(),
//                    thread.getId()
//            ));
//        }

        return new ThreadResponse(
                thread.getId(),
                threadTitle,
                thread.getCompletionModel(),
                thread.isHasUnreadMessages(),
                messageDtos,
                thread.getCreatedAt(),
                thread.getUpdatedAt()
        );
    }

    @Transactional
    public void markThreadAsRead(Long threadId, Authentication authentication) throws BootcampException {
        User currentUser = getCurrentUser(authentication);

        Thread thread = threadRepository.findByIdAndUserId(threadId, currentUser.getId())
                .orElseThrow(() -> new BootcampException(HttpStatus.NOT_FOUND, "Thread not found or not owned by user"));

////        Long userId = currentUser.getId();
//
//        Thread thread = threadRepository.findById(threadId)
//                .orElseThrow(() -> new BootcampException(HttpStatus.NOT_FOUND, "Thread not found with id: " + threadId));

        if (thread.isHasUnreadMessages()) {
            thread.setHasUnreadMessages(false);
            thread.setUpdatedAt(Instant.now());
            threadRepository.save(thread);
        }
    }
}