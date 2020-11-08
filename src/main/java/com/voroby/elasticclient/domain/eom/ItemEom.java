package com.voroby.elasticclient.domain.eom;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.UUID;

@Document(indexName = "items", createIndex = false)
@TypeAlias("item")
@Getter
@Setter
public class ItemEom {
    @Id
    private String id;
    private String name;
    private String description;
    private UserEom owner;

    public ItemEom() {
        this.id = UUID.randomUUID().toString();
    }

    public ItemEom(String name, String description, UserEom owner) {
        this();
        this.name = name;
        this.description = description;
        this.owner = owner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ItemEom item = (ItemEom) o;

        if (!id.equals(item.id)) return false;
        if (!name.equals(item.name)) return false;
        return description != null ? description.equals(item.description) : item.description == null;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }
}
