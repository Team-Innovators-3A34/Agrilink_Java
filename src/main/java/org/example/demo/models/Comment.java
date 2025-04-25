package org.example.demo.models;

public class Comment {
    private int id;
    private int post_id_id;
    private String content;
    private int user_commented_id;
    private String created_at;

    // Add reference to Posts
    private Posts post;

    public Posts getPost() {
        return post;
    }

    public void setPost(Posts post) {
        this.post = post;
    }

    public Comment() {
    }

    public Comment(int id, int post_id_id, String content, int user_commented_id, String created_at) {
        this.id = id;
        this.post_id_id = post_id_id;
        this.content = content;
        this.user_commented_id = user_commented_id;
        this.created_at = created_at;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPost_id_id() {
        return post_id_id;
    }

    public void setPost_id_id(int post_id_id) {
        this.post_id_id = post_id_id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getUser_commented_id() {
        return user_commented_id;
    }

    public void setUser_commented_id (Integer user_commented_id){
        this.user_commented_id = user_commented_id;
    }


    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", post_id_id=" + post_id_id +
                ", content='" + content + '\'' +
                ", user_commented_id='" + user_commented_id + '\'' +
                ", created_at='" + created_at + '\'' +
                '}';
    }
}
