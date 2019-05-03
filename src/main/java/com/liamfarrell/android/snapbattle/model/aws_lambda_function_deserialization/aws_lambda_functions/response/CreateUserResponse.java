package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response;

import java.util.List;

public class CreateUserResponse {
    private String user_exists;
    private String username;
    private List<String> messages;
    private String name;

    public String getUserExists() {
        return user_exists;
    }

    public void setUserExists(String user_exists) {
        this.user_exists = user_exists;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
