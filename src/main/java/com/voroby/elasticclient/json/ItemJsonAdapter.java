package com.voroby.elasticclient.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.voroby.elasticclient.domain.Item;

import java.lang.reflect.Type;

public class ItemJsonAdapter implements JsonSerializer<Item> {
    @Override
    public JsonElement serialize(Item item, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", item.getId());
        jsonObject.addProperty("name", item.getName());
        jsonObject.addProperty("description", item.getDescription());
        jsonObject.addProperty("owner", item.getOwner().getId());

        return jsonObject;
    }
}
