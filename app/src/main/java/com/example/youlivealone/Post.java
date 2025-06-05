package com.example.youlivealone;

import java.io.Serializable;

public class Post implements Serializable {
    private String community_postid;
    private int userid;
    private String title;
    private String content;
    private int categoryId;
    private int likes;


    public Post( String id, int userId, String title, String content,int categoryId,int likes) {
        this.community_postid=id;
        this.userid = userId;
        this.title = title;
        this.content = content;
        this.categoryId = categoryId;
        this.likes = likes;
    }

    // Getters
    public String getId() {
        return community_postid;
    }
    public int getUserId() {
        return userid;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return title; // 또는 필요한 내용을 반환
    }
    public int getLikes() {
        return likes;
    }
    public int getCategoryId() {
        return categoryId;
    }
}
