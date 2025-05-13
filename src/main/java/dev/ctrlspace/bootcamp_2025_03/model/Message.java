package dev.ctrlspace.bootcamp_2025_03.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {

    public long id;
    public String content;
    public long threadId;

    public Boolean isCompletion;
    public String completionModel;

}
