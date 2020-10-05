package com.voroby.elasticclient.domain;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class User {
    private String id;
    private String email;
    private String password;
    private LocalDateTime created;
    private Set<Item> items;

    public User() {
        this.id = UUID.randomUUID().toString();
        this.created = LocalDateTime.now();
    }

    public User(String email, String password) {
        this();
        this.email = email;
        this.password = password;
        this.items = new HashSet<>();
    }

    private void setId(String id) {}
}
