package dev.ctrlspace.bootcamp_2025_03.controllers;

import dev.ctrlspace.bootcamp_2025_03.model.Message;
import dev.ctrlspace.bootcamp_2025_03.services.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessagesController {

    private MessageService messageService;


    @Autowired
    public MessagesController(MessageService messageService) {
        this.messageService = messageService;
    }


    //CRUD /messages
    // GET all messages by thread ID

    @PostMapping("/messages")
    public Message createMessage(@RequestBody Message message) {


        //return response from LLM

        Message responseMessage = messageService.createMessageAndGetCompletion(message);


        return responseMessage;

    }
}
