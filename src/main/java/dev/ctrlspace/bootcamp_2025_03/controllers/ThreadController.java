package dev.ctrlspace.bootcamp_2025_03.controllers;

import dev.ctrlspace.bootcamp_2025_03.exceptions.BootcampException;
import dev.ctrlspace.bootcamp_2025_03.model.Thread;
import dev.ctrlspace.bootcamp_2025_03.model.dto.ThreadRequest;
import dev.ctrlspace.bootcamp_2025_03.model.dto.ThreadResponse;
import dev.ctrlspace.bootcamp_2025_03.services.ThreadService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ThreadResponse createThread(@RequestBody ThreadRequest request) {
        return threadService.createThread(request);
    }

    @GetMapping("/threads/{id}")
    public ThreadResponse getThreadById(@PathVariable("id") Long id) throws BootcampException {
        threadService.markThreadAsRead(id);
        return threadService.getThreadById(id);
    }

    @GetMapping("/threads")
    public List<ThreadResponse> getAllThreads() {
        return threadService.getAllThreads();
    }

    @PutMapping("/threads/{id}")
    public Thread updateThread(@PathVariable("id") Long id, @RequestBody Thread thread) throws BootcampException {
        return threadService.updateThread(id, thread);
    }

    @DeleteMapping("/threads/{id}")
    public void deleteThread(@PathVariable("id") Long id) throws BootcampException {
        threadService.deleteThread(id);
    }
}
