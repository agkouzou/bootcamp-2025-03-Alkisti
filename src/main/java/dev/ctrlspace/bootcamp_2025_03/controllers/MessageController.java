package dev.ctrlspace.bootcamp_2025_03.controllers;

import dev.ctrlspace.bootcamp_2025_03.exceptions.BootcampException;
import dev.ctrlspace.bootcamp_2025_03.model.dto.MessageRequest;
import dev.ctrlspace.bootcamp_2025_03.model.dto.MessageResponse;
import dev.ctrlspace.bootcamp_2025_03.model.dto.ThreadResponse;
import dev.ctrlspace.bootcamp_2025_03.services.MessageService;
import dev.ctrlspace.bootcamp_2025_03.services.ThreadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MessageController {

    private MessageService messageService;
    private ThreadService threadService;

    @Autowired
    public MessageController(MessageService messageService, ThreadService threadService) {
        this.messageService = messageService;
        this.threadService = threadService;
    }

    @PostMapping("/threads/{threadId}/messages")
    public ThreadResponse createMessage(@PathVariable Long threadId, @RequestBody MessageRequest request, Authentication authentication) throws BootcampException {
        request.setThreadId(threadId);
        messageService.createMessageAndGetCompletion(request, authentication);

        // Return updated thread (with messages and possibly updated title)
        return threadService.getThreadById(threadId, authentication);
    }

    @GetMapping("/messages/{id}")
    public MessageResponse getMessageById(@PathVariable("id") Long id, Authentication authentication) throws BootcampException {
        return messageService.getMessageById(id, authentication);
    }

    @GetMapping("/threads/{threadId}/messages")
    public List<MessageResponse> getMessagesByThreadId(@PathVariable Long threadId, Authentication authentication) throws BootcampException {
        return messageService.getMessagesByThreadId(threadId, authentication);
    }

    @PutMapping("/messages/{id}")
    public MessageResponse updateMessage(@PathVariable("id") Long id, @RequestBody MessageRequest request, Authentication authentication) throws BootcampException {
        return messageService.updateMessage(id, request, authentication);
    }

    @DeleteMapping("/messages/{id}")
    public void deleteMessage(@PathVariable("id") Long id, Authentication authentication) throws BootcampException {
        messageService.deleteMessage(id, authentication);
    }
}
