package com.voroby.elasticclient.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.voroby.elasticclient.domain.User;

import java.lang.reflect.Type;
import java.time.format.DateTimeFormatter;

public class UserJsonAdapter implements JsonSerializer<User> {
    @Override
    public JsonElement serialize(User user, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", user.getId());
        jsonObject.addProperty("email", user.getEmail());
        jsonObject.addProperty("password", user.getPassword());
        jsonObject.addProperty("created", user.getCreated().format(DateTimeFormatter.ofPattern("yyyy.MM.dd hh:mm:ss")));
        JsonObject items = new JsonObject();
        user.getItems().forEach(item -> {
            items.addProperty("id", item.getId());
            items.addProperty("name", item.getName());
            items.addProperty("description", item.getDescription());
            items.addProperty("ownerID", item.getOwner().getId());
        });
        jsonObject.add("items", items);

        return jsonObject;
    }
}