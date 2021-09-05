package com.example.snakejava;



public class User {

    private String email;
    private String password;
    private String nickname;
    private String score;

    public User() {
    }

    public User(String nickname, String email){
        this.nickname = nickname;
        this.email = email;

    }

    public User(String email, String password, String nickname, String score) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.score = score;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getNickname() {
        return nickname;
    }

    public String getScore() {
        return score;
    }


}
