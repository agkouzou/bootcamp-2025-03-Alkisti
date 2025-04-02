package dev.ctrlspace.bootcamp_2025_03.web_simpe_java_examples.day_3.model;

/**
 * This class represents a message thread.
 * A message thread is a collection of messages, sent by a user to an LLM, and also contains the response messages from the LLM
 * A thread also is described by its settings.
 */
public class MessageThread {

    private String threadName;

    private Message[] messages;

}
