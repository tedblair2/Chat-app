package com.example.whatsappclone.Model;

public class Users {
    private String id;
    private String name;
    private String username;
    private String imageurl;
    private String email;
    private String status;

    public Users() {
    }

    public Users(String id, String name, String username, String imageurl, String email, String status) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.imageurl = imageurl;
        this.email = email;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
