package com.ceng495.hw2.model;

public class GameComment {

    private String username;

    private String comment;

    public GameComment(String username, String comment) {
        this.username = username;
        this.comment = comment;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
