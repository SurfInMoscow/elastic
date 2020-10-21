package com.voroby.elasticclient;

import com.voroby.elasticclient.domain.User;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UpdateElasticTest extends AbstractElasticTest {
    @Test
    public void updateRequest() throws IOException {
        User user = users.iterator().next();
        GetRequest userReq = new GetRequest("users", user.getId());

        GetResponse userResponse = restHighLevelClient.get(userReq, RequestOptions.DEFAULT);
        long version = userResponse.getVersion();

        Map<String, Object> jsonMap = new HashMap<>();
        user.setEmail("newmail@gmail.com");
        jsonMap.put("email", user.getEmail());
        UpdateRequest updateRequest = new UpdateRequest("users", user.getId()).doc(jsonMap);
        UpdateResponse updateResponse = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);

        assertEquals(200, updateResponse.status().getStatus());
        version++;
        assertEquals(version, updateResponse.getVersion());
    }
}
