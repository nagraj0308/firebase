package com.nagraj.firebase;

import java.io.Serializable;

class User implements Serializable {
    public String username;
    public String url;

    public User() {
    }
    public User(String username, String url) {
        this.username = username;
        this.url = url;
    }

}
