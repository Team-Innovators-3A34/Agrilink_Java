package org.example.demo.models;

public class Reaction {
    private int id;
    private int post_id;
    private int user_id;
    private String type;
    private String created_at;


    private Posts post;

    public Reaction() {
    }

    public Reaction(int id, int post_id, int user_id, String type, String created_at) {
        this.id = id;
        this.post_id = post_id;
        this.user_id = user_id;
        this.type = type;
        this.created_at = created_at;
    }

    public Posts getPost() {
        return post;
    }

    public void setPost(Posts post) {
        this.post = post;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPost_id() {
        return post_id;
    }

    public void setPost_id(int post_id) {
        this.post_id = post_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    @Override
    public String toString() {
        return "Reaction{" +
                "id=" + id +
                ", post_id=" + post_id +
                ", user_id=" + user_id +
                ", type='" + type + '\'' +
                ", created_at='" + created_at + '\'' +
                '}';
    }
}
