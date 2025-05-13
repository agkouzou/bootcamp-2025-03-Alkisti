package dev.ctrlspace.bootcamp_2025_03.services;

import dev.ctrlspace.bootcamp_2025_03.model.Message;
import dev.ctrlspace.bootcamp_2025_03.model.dto.ChatCompletionRequest;
import dev.ctrlspace.bootcamp_2025_03.model.dto.ChatCompletionResponse;
import dev.ctrlspace.bootcamp_2025_03.model.dto.ChatMessage;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class MessageService {

private String groqApiKey = "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";


    public Message createMessageAndGetCompletion(Message newMessage) {

        // save new message in DB

        RestTemplate restTemplate = new RestTemplate();

        //set header bearer
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + groqApiKey);

        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setModel(newMessage.getCompletionModel());
        ChatMessage systemMessage = new ChatMessage("system", "You are a helpful assistant.");
        ChatMessage userMessage = new ChatMessage("user", newMessage.getContent());

        chatCompletionRequest.setMessages(List.of(systemMessage, userMessage));

        // 3) wrap both body & headers
        HttpEntity<ChatCompletionRequest> httpEntity =
                new HttpEntity<>(chatCompletionRequest, headers);

        // set body
        ChatCompletionResponse response = restTemplate.postForEntity(
                "https://api.groq.com/openai/v1/chat/completions",
                httpEntity,
                ChatCompletionResponse.class
        ).getBody();

        Message responseMessage = new Message();
        responseMessage.setIsCompletion(true);
        responseMessage.setContent(response.getChoices().get(0).getMessage().getContent());
        responseMessage.setCompletionModel(response.getModel());
        responseMessage.setThreadId(newMessage.getThreadId());


        // save responseMessage message in DB

        // return completion message

        return responseMessage;
    }
}
