package dev.ctrlspace.bootcamp_2025_03.controllers;

import dev.ctrlspace.bootcamp_2025_03.exceptions.BootcampException;
import dev.ctrlspace.bootcamp_2025_03.model.Thread;
import dev.ctrlspace.bootcamp_2025_03.model.dto.ThreadRequest;
import dev.ctrlspace.bootcamp_2025_03.model.dto.ThreadResponse;
import dev.ctrlspace.bootcamp_2025_03.services.ThreadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ThreadController {

    private ThreadService threadService;

    @Autowired
    public ThreadController(ThreadService threadService) {
        this.threadService = threadService;
    }

    @PostMapping("/threads")
    public ThreadResponse createThread(@RequestBody ThreadRequest request, Authentication authentication) throws BootcampException{
        return threadService.createThread(request, authentication);
    }

    @GetMapping("/threads/{id}")
    public ThreadResponse getThreadById(@PathVariable("id") Long id, Authentication authentication) throws BootcampException {
        return threadService.getThreadById(id, authentication);
    }

    @GetMapping("/threads")
    public List<ThreadResponse> getAllThreads(Authentication authentication) throws BootcampException{
        return threadService.getAllThreads(authentication);
    }

    @PutMapping("/threads/{id}")
    public Thread updateThread(@PathVariable("id") Long id, @RequestBody Thread thread, Authentication authentication) throws BootcampException {
        return threadService.updateThread(id, thread, authentication);
    }

    @DeleteMapping("/threads/{id}")
    public void deleteThread(@PathVariable("id") Long id, Authentication authentication) throws BootcampException {
        threadService.deleteThread(id, authentication);
    }
}
