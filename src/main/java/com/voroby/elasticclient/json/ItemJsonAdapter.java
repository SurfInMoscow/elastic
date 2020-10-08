package com.voroby.elasticclient.json;

import com.google.gson.*;
import com.voroby.elasticclient.domain.Item;

import java.lang.reflect.Type;

public class ItemJsonAdapter implements JsonSerializer<Item>, JsonDeserializer<Item> {
    @Override
    public JsonElement serialize(Item item, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", item.getId());
        jsonObject.addProperty("name", item.getName());
        jsonObject.addProperty("description", item.getDescription());
        jsonObject.addProperty("owner", item.getOwner().getId());

        return jsonObject;
    }

    @Override
    public Item deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Item item = new Item();
        item.setId(json.getAsJsonObject().get("id").getAsString());
        item.setName(json.getAsJsonObject().get("name").getAsString());
        item.setDescription(json.getAsJsonObject().get("description").getAsString());

        return item;
    }
}
