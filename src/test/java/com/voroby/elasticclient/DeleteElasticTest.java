package com.voroby.elasticclient;

import com.voroby.elasticclient.domain.Item;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.client.RequestOptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeleteElasticTest extends AbstractElasticTest {
    @Test
    public void deleteRequest() throws IOException {
        Item item = items.iterator().next();
        DeleteRequest delete = new DeleteRequest("items", item.getId());
        DeleteResponse deleteResponse = restHighLevelClient.delete(delete, RequestOptions.DEFAULT);
        assertEquals(200, deleteResponse.status().getStatus());
    }

    @Test
    public void deleteNotExist() throws IOException {
        Item mockItem = mock(Item.class);
        when(mockItem.getId()).thenReturn(UUID.randomUUID().toString());
        DeleteRequest delete = new DeleteRequest("items", mockItem.getId());
        DeleteResponse deleteResponse = restHighLevelClient.delete(delete, RequestOptions.DEFAULT);
        assertEquals(404, deleteResponse.status().getStatus());
        assertEquals("not_found", deleteResponse.getResult().getLowercase());
    }
}
