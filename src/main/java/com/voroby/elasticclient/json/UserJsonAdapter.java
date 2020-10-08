package com.voroby.elasticclient.json;

import com.google.gson.*;
import com.voroby.elasticclient.domain.Item;
import com.voroby.elasticclient.domain.User;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

public class UserJsonAdapter implements JsonSerializer<User>, JsonDeserializer<User> {
    @Override
    public JsonElement serialize(User user, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", user.getId());
        jsonObject.addProperty("email", user.getEmail());
        jsonObject.addProperty("password", user.getPassword());
        jsonObject.addProperty("created", user.getCreated().format(DateTimeFormatter.ofPattern("yyyy.MM.dd hh:mm:ss")));

        return jsonObject;
    }

    @Override
    public User deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        User user = new User();
        user.setId(json.getAsJsonObject().get("id").getAsString());
        user.setEmail(json.getAsJsonObject().get("email").getAsString());
        user.setPassword(json.getAsJsonObject().get("password").getAsString());
        //TODO
        user.setCreated(extractLocalDateTime(null));

        final JsonArray items = json.getAsJsonObject().getAsJsonArray("items");
        Set<Item> itemSet = new HashSet<>();
        items.forEach(jsonElement -> {
            Item item = new Item();
            item.setId(jsonElement.getAsJsonObject().get("id").getAsString());
            item.setName(jsonElement.getAsJsonObject().get("name").getAsString());
            item.setDescription(jsonElement.getAsJsonObject().get("description").getAsString());
            itemSet.add(item);
        });
        user.setItems(itemSet);

        return user;
    }

    private LocalDateTime extractLocalDateTime(JsonElement element) {
        //TODO
        return null;
    }
}