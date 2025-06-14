package dev.ctrlspace.bootcamp_2025_03.services;

import dev.ctrlspace.bootcamp_2025_03.exceptions.BootcampException;
import dev.ctrlspace.bootcamp_2025_03.model.Message;
import dev.ctrlspace.bootcamp_2025_03.model.Thread;
import dev.ctrlspace.bootcamp_2025_03.model.User;
import dev.ctrlspace.bootcamp_2025_03.model.dto.*;
import dev.ctrlspace.bootcamp_2025_03.repository.MessageRepository;
import dev.ctrlspace.bootcamp_2025_03.repository.ThreadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MessageService {

    @Value("${groq.api.key}")
    private String groqApiKey;

    @Value("${groq.model}")
    private String groqDefaultModel;

    @Value("${groq.api.url}")
    private String groqApiUrl;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ThreadRepository threadRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserProfileSettingsService userProfileSettingsService;

    public void checkThreadOwnership(Long threadId, Authentication authentication) throws BootcampException {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String userIdStr = jwt.getClaim("sub");
        Long userId = Long.parseLong(userIdStr);

        boolean isOwner = threadRepository.existsByIdAndUser_Id(threadId, userId);
        if (!isOwner) {
            throw new BootcampException(HttpStatus.FORBIDDEN, "You do not have access to this thread.");
        }
    }

    public MessageResponse createMessageAndGetCompletion(MessageRequest request, Authentication authentication) throws BootcampException {
        Thread thread;

        try {
            Optional<Thread> threadOpt = threadRepository.findById(request.getThreadId());
            if (threadOpt.isEmpty()) {
                throw new BootcampException(HttpStatus.NOT_FOUND, "Thread not found with ID: " + request.getThreadId());
            }

            thread = threadOpt.get();

            checkThreadOwnership(thread.getId(), authentication);

            String model = (request.getCompletionModel() != null && !request.getCompletionModel().isBlank())
                    ? request.getCompletionModel()
                    : groqDefaultModel;

            model = model.toLowerCase();

            // Step 1: Create and save the new user message
            Message newMessage = new Message();
            newMessage.setContent(request.getContent());
            newMessage.setIsCompletion(false);
            newMessage.setCompletionModel(model);
            newMessage.setThread(thread);
            newMessage.setCreatedAt(Instant.now());
            newMessage.setUpdatedAt(Instant.now());

            messageRepository.save(newMessage);

            // Step 2: Set the model if not already set in the thread
            if (thread.getCompletionModel() == null || thread.getCompletionModel().isBlank()) {
                thread.setCompletionModel(model);
            }

            // Step 3: Fetch updated history after saving user message
            List<Message> threadHistory = messageRepository.findByThreadIdOrderByIdAsc(thread.getId());

            // Step 4: Generate assistant's response
            Message responseMessage = generateCompletion(threadHistory, model, thread);

            // Step 5: Prepare updated history including assistant response
            List<Message> updatedHistory = new ArrayList<>(threadHistory);
            updatedHistory.add(responseMessage);

            // Step 6: If this is the first full pair (user + assistant)
            if (updatedHistory.size() == 2) {
                Message userMsg = updatedHistory.get(0);
                Message assistantMsg = updatedHistory.get(1);

                if (Boolean.FALSE.equals(userMsg.getIsCompletion()) && Boolean.TRUE.equals(assistantMsg.getIsCompletion())) {
                    try {
                        // Step 7: Generate the thread title based on user and assistant messages

                        // 7a. Fetch user ID from authentication or context (if not already available)
                        Jwt jwt = (Jwt) authentication.getPrincipal();
                        String userIdStr = jwt.getClaim("sub");
                        Long userId = Long.parseLong(userIdStr);

                        // 7b. Fetch user profile settings for the user
                        UserProfileSettingsDTO settings = userProfileSettingsService.getProfileSettingsByUserId(userId);

                        // 7c. Pass settings when generating thread title
                        String generatedTitle = generateThreadTitle(userMsg.getContent(), assistantMsg.getContent(), model, settings);

                        if (generatedTitle == null || generatedTitle.isBlank()) {
                            generatedTitle = "New Chat";
                        }

                        thread.setTitle(generatedTitle);
                        threadRepository.save(thread); // Save updated title

                    } catch (Exception e) {
                        e.printStackTrace(); // Fail gracefully
                    }
                }
            }

            threadRepository.save(thread);
            return toDto(responseMessage);

        } catch (BootcampException be) {
            throw be;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BootcampException("Error while handling message: " + e.getMessage());
        }
    }

    public MessageResponse updateMessage(Long id, MessageRequest updatedMessage, Authentication authentication) throws BootcampException {
        if (updatedMessage.getContent() == null || updatedMessage.getContent().trim().isEmpty()) {
            throw new BootcampException(HttpStatus.BAD_REQUEST, "Message content cannot be empty.");
        }

        Message existing = messageRepository.findById(id)
                .orElseThrow(() -> new BootcampException(HttpStatus.NOT_FOUND, "Message not found with id: " + id));

        checkThreadOwnership(existing.getThread().getId(), authentication);

        existing.setContent(updatedMessage.getContent());
        existing.setCompletionModel(
                updatedMessage.getCompletionModel() != null && !updatedMessage.getCompletionModel().isBlank()
                        ? updatedMessage.getCompletionModel()
                        : existing.getCompletionModel()
        );
        existing.setUpdatedAt(Instant.now());
        messageRepository.save(existing);

        if (Boolean.TRUE.equals(updatedMessage.getRegenerate()) && !existing.getIsCompletion()) {
            List<Message> threadHistory = messageRepository.findByThreadIdOrderByIdAsc(existing.getThread().getId());

            // Delete the assistant message right after the user message being regenerated
            Message previousCompletion = null;
            for (int i = 0; i < threadHistory.size(); i++) {
                if (threadHistory.get(i).getId() == existing.getId() && i + 1 < threadHistory.size()) {
                    Message next = threadHistory.get(i + 1);
                    if (Boolean.TRUE.equals(next.getIsCompletion())) {
                        previousCompletion = next;
                        break;
                    }
                }
            }

            if (previousCompletion != null) {
                messageRepository.delete(previousCompletion);
            }

            // Limit context to messages up to and including the updated user message
            List<Message> boundedHistory = new ArrayList<>();
            for (Message m : threadHistory) {
                boundedHistory.add(m);
                if (m.getId() == existing.getId()) break;
            }

            Message responseMessage = generateCompletion(boundedHistory, existing.getCompletionModel(), existing.getThread());

            return toDto(responseMessage);
        }

        return toDto(existing);
    }

    public List<MessageResponse> getMessagesByThreadId(Long threadId, Authentication authentication) throws BootcampException {
        if (!threadRepository.existsById(threadId)) {
            throw new BootcampException(HttpStatus.NOT_FOUND, "Thread not found with ID: " + threadId);
        }

        Thread thread = threadRepository.findById(threadId).orElseThrow(() ->
                new BootcampException(HttpStatus.NOT_FOUND, "Thread not found with ID: " + threadId)
        );

        checkThreadOwnership(thread.getId(), authentication);

        List<Message> messages = messageRepository.findByThreadIdOrderByIdAsc(threadId);
        List<MessageResponse> result = new ArrayList<>();
        for (Message m : messages) {
            result.add(toDto(m));
        }

        return result;
    }

    public MessageResponse getMessageById(Long id, Authentication authentication) throws BootcampException {
        Message m = messageRepository.findById(id)
                .orElseThrow(() -> new BootcampException(HttpStatus.NOT_FOUND, "Message not found with id: " + id));

        checkThreadOwnership(m.getThread().getId(), authentication);

        return toDto(m);
    }

    public void deleteMessage(Long id, Authentication authentication) throws BootcampException {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new BootcampException(HttpStatus.NOT_FOUND, "Message not found with id: " + id));

        checkThreadOwnership(message.getThread().getId(), authentication);

        if (!message.getIsCompletion()) {
            // If it's a user message, find and delete its assistant response
            Optional<Message> assistantReply = findAssistantResponseFor(message);
            assistantReply.ifPresent(reply -> messageRepository.deleteById(reply.getId()));
        }

        messageRepository.deleteById(id);
    }

    // Shared logic for generating assistant completions
    public Message generateCompletion(List<Message> history, String model, Thread thread) throws BootcampException {

        // save new message in DB

        RestTemplate restTemplate = new RestTemplate();

        // Set header bearer
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + groqApiKey);

        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setModel(model);

        // Include system prompt with the actual current date + full message history
        String today = java.time.LocalDate.now().toString();

        // Fetch user profile settings
        Long userId = thread.getUser().getId(); // thread already contains user
        User user = userService.getUserById(userId);
        UserProfileSettingsDTO settings = userProfileSettingsService.getProfileSettingsByUser(user);

        String nickname = settings.getNickname();
        String job = settings.getJob();
        List<String> traitsList = settings.getTraits();
        String traits = traitsList != null ? String.join(", ", traitsList) : "";
        String intro = settings.getIntroduction();
        String notes = settings.getNotes();

        // Build system prompt using profile
        String systemPrompt = String.format("""
        You are a helpful and up-to-date assistant. Today’s date is %s.
        Always address the user as '%s'.
        The user is a '%s'. Their intro: "%s".
        Embody the following traits: %s.
        Notes: %s
        Use the conversation history to stay consistent.
        Avoid mentioning training data limitations or knowledge cutoffs.
        """, today, nickname, job, intro, traits, notes);

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("system", systemPrompt));

        // Convert full history to OpenAI-style messages
        for (Message m : history) {
            if (m.getContent() == null || m.getContent().trim().isEmpty()) {
                continue; // Skip invalid messages
            }
            String role = Boolean.TRUE.equals(m.getIsCompletion()) ? "assistant" : "user";
            messages.add(new ChatMessage(role, m.getContent()));
        }

        chatCompletionRequest.setMessages(messages);

        // Wrap body & headers
        HttpEntity<ChatCompletionRequest> httpEntity = new HttpEntity<>(chatCompletionRequest, headers);

        // Make request
        ChatCompletionResponse response = restTemplate.postForEntity(
                groqApiUrl,
                httpEntity,
                ChatCompletionResponse.class
        ).getBody();

        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            throw new BootcampException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid response from Groq API.");
        }

        Message responseMessage = new Message();
        responseMessage.setIsCompletion(true);
        responseMessage.setContent(response.getChoices().get(0).getMessage().getContent());
        responseMessage.setCompletionModel(response.getModel());
        responseMessage.setThread(thread);
        responseMessage.setCreatedAt(Instant.now());
        responseMessage.setUpdatedAt(Instant.now());

        // Save responseMessage message in DB

        messageRepository.save(responseMessage);

        // Return completion message

        return responseMessage;
    }

    private String generateThreadTitle(String newMessage, String responseMessage, String model, UserProfileSettingsDTO settings) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + groqApiKey);

            ChatCompletionRequest request = new ChatCompletionRequest();
            request.setModel(model);

            List<ChatMessage> messages = new ArrayList<>();

            // Build a personalized system prompt based on user profile settings
            StringBuilder systemPrompt = new StringBuilder("You are a helpful assistant. Generate a short, 5-7 word title that summarizes the following conversation. ");

            if (settings.getNickname() != null && !settings.getNickname().isBlank()) {
                systemPrompt.append("The user’s nickname is ").append(settings.getNickname()).append(". ");
            }

            if (settings.getIntroduction() != null && !settings.getIntroduction().isBlank()) {
                systemPrompt.append("Intro: ").append(settings.getIntroduction()).append(" ");
            }

            if (settings.getJob() != null && !settings.getJob().isBlank()) {
                systemPrompt.append("The user works as a ").append(settings.getJob()).append(". ");
            }

            if (settings.getTraits() != null && !settings.getTraits().isEmpty()) {
                String traitsJoined = String.join(", ", settings.getTraits());
                systemPrompt.append("Personality traits: ").append(traitsJoined).append(". ");
            }

            if (settings.getNotes() != null && !settings.getNotes().isBlank()) {
                systemPrompt.append("Additional notes: ").append(settings.getNotes()).append(". ");
            }

            messages.add(new ChatMessage("system", systemPrompt.toString()));

            messages.add(new ChatMessage("user", "User: " + newMessage + "\nAssistant: " + responseMessage + "\n\nTitle:"));

            request.setMessages(messages);

            HttpEntity<ChatCompletionRequest> httpEntity = new HttpEntity<>(request, headers);

            ChatCompletionResponse response = restTemplate.postForEntity(
                    groqApiUrl,
                    httpEntity,
                    ChatCompletionResponse.class
            ).getBody();

            if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
                return "New Chat Thread";
            }

            String generatedTitle = response.getChoices().get(0).getMessage().getContent().trim();

            // Remove leading/trailing quotes if present
            if (generatedTitle.startsWith("\"") && generatedTitle.endsWith("\"") && generatedTitle.length() > 1) {
                generatedTitle = generatedTitle.substring(1, generatedTitle.length() - 1).trim();
            }

            return (generatedTitle == null || generatedTitle.isBlank()) ? "New Chat Thread" : generatedTitle;

        } catch (Exception e) {
            e.printStackTrace();
            return "New Chat Thread";
        }
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

    public Optional<Message> findAssistantResponseFor(Message userMessage) {
        return messageRepository.findFirstByThreadIdAndUpdatedAtAfterAndIsCompletionTrueOrderByUpdatedAtAsc(
                userMessage.getThread().getId(), userMessage.getUpdatedAt()
        );
    }
}
