package com.voroby.elasticclient.domain.eom;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Document(indexName = "users", createIndex = false)
@TypeAlias("user")
@Getter
@Setter
public class UserEom {
    @Id
    private String id;
    private String email;
    private String password;
    @Field(format = DateFormat.date_time)
    private LocalDateTime created;
    @Transient
    private Set<ItemEom> items;

    public UserEom() {
        this.id = UUID.randomUUID().toString();
        this.created = LocalDateTime.now();
    }

    public UserEom(String email, String password) {
        this();
        this.email = email;
        this.password = password;
        this.items = new HashSet<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserEom user = (UserEom) o;

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
