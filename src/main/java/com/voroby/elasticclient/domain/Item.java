package com.voroby.elasticclient.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class Item {
    private String id;
    private String name;
    private String description;
    private User owner;

    public Item() {
        this.id = UUID.randomUUID().toString();
    }

    public Item(String name, String description, User owner) {
        this();
        this.name = name;
        this.description = description;
        this.owner = owner;
    }

    private void setId(String id) {}
}
