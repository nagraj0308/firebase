package com.nagraj.firebase;

import java.io.Serializable;

public class Upload implements Serializable {
    public String username;
    public String url;

    public Upload() {

    }
    public Upload(String username, String url) {
        this.username = username;
        this.url = url;
    }
}
