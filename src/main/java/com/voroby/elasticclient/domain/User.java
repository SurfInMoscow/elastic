package com.voroby.elasticclient.domain;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (!id.equals(user.id)) return false;
        if (!email.equals(user.email)) return false;
        if (!password.equals(user.password)) return false;
        return created.equals(user.created);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + email.hashCode();
        result = 31 * result + password.hashCode();
        result = 31 * result + created.hashCode();
        return result;
    }
}
